package visitor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class DependentSetDetector extends ASTVisitor {
	
	private Set<String> dependentClassSet = new HashSet<String>();
	
	public Set<String> getDependentClassSet() {
		return dependentClassSet;
	}
	
	public DependentSetDetector(MethodDeclaration[] decList, FieldDeclaration[] fieldList) {
		FieldVisitor visitor = new FieldVisitor();
		for (FieldDeclaration field : fieldList) {
			field.accept(visitor);
		}
		
		for (MethodDeclaration dec : decList) {
			dec.accept(this);
		}
	}
	
	private Set<String> fieldNameSet = new HashSet<String>(); 
	
	class FieldVisitor extends ASTVisitor {
		
		@Override
		public boolean visit(VariableDeclarationFragment node) {
			fieldNameSet.add(node.getName().toString());
			return true;
		}
		
		/**
		 * class instance creation
		 */
		@Override
		public boolean visit(ClassInstanceCreation node) {
			dependentClassSet.add(
					getTypeName(
							node.getType()));
			return true;
		}
		
		/**
		 * type literal
		 */
		@Override
		public boolean visit(TypeLiteral node) {
			dependentClassSet.add(
					getTypeName(
							node.getType()));
			return true;
		}
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		for (Object o: node.thrownExceptions()) {
			String name = getName((Name) o);
			dependentClassSet.add(name);
		}
		
		for (Object o: node.parameters()) {
			SingleVariableDeclaration param = (SingleVariableDeclaration)o;
			String typeName = getTypeName(param.getType());
			if (typeName != null) {
				dependentClassSet.add(typeName);
			}
		}
		
		Block body = node.getBody();
		if (body != null) {
			for (Object o: node.getBody().statements()) {
				Statement statement = (Statement)o;
				statement.accept(new MethodVisitor());
			}
		}
		
		return true;
	}
	
	class MethodVisitor extends ASTVisitor {
		
		/**
		 * static method invocation
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			Expression expression = node.getExpression();
			if (expression != null && expression instanceof Name) {
				String name = getName((Name) expression);
				if (!fieldNameSet.contains(name)) {
					dependentClassSet.add(name);
				}
			}
			
			return true;
		}
		
		/**
		 * static field access
		 */
		@Override
		public boolean visit(QualifiedName node) {
			String name = getName(node.getQualifier());
			if (!fieldNameSet.contains(name)) {
				dependentClassSet.add(name);
			}
			
			return true;
		}
		
		/**
		 * class instance creation
		 */
		@Override
		public boolean visit(ClassInstanceCreation node) {
			dependentClassSet.add(
					getTypeName(
							node.getType()));
			return true;
		}
		
		/**
		 * catch exception
		 */
		@Override
		public boolean visit(CatchClause node) {
			dependentClassSet.add(
					getTypeName(
							node.getException().getType()));
			return true;
		}
		
		/**
		 * instance of
		 */
		@Override
		public boolean visit(InstanceofExpression node) {
			dependentClassSet.add(
					getTypeName(
							node.getRightOperand()));
			return true;
		}
		
		/**
		 * type literal
		 */
		@Override
		public boolean visit(TypeLiteral node) {
			dependentClassSet.add(
					getTypeName(
							node.getType()));
			return true;
		}
		
	}
	
	private String getName(Name node) {
		//if it's parameterized, get the argument type and continue judgment
		if (node.isQualifiedName()) {
			QualifiedName name = (QualifiedName) node;
			return getName(name.getQualifier());
		}
		//if it's a simple name
		else if (node.isSimpleName()) {
			return node.toString();
		}
		
		return null;
	}
	
	private String getTypeName(Type node) {
		//if it's a array , get the component type and continue judgment
		if (node.isArrayType()) {
			ArrayType type = (ArrayType) node;
			return getTypeName(type.getComponentType());
		}
		//if it's parameterized, get the argument type and continue judgment
		else if (node.isParameterizedType()) {
			ParameterizedType type = (ParameterizedType) node;
			for (Object o: type.typeArguments()) {
				Type t = (Type)o;
				return getTypeName(t);
			}
		}
		//if it's a simple type
		else if (node.isSimpleType()) {
			return node.toString();
		}
		
		return null;
	}
}
