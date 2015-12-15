package visitor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class InheritanceSetDetector extends ASTVisitor {
	
	private Set<String> superClassSet = new HashSet<String>();
	
	public Set<String> getSuperClassSet() {
		return superClassSet;
	}
	
	public InheritanceSetDetector(TypeDeclaration node) {
		node.accept(this);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		//if it has super class
		if (node.getSuperclassType() != null) {
			superClassSet.add(
					getTypeName(
							node.getSuperclassType()));
		}
		//if it has interface
		for (Object i: node.superInterfaceTypes()) {
			superClassSet.add(
					getTypeName(
							((Type)i)));
		}
		
		return false;
	}
	
	private String getTypeName(Type node) {
		String superClass = node.toString();
		
		//if it's parameterized, get the argument type and continue judgment
		if (node.isParameterizedType()) {
			ParameterizedType type = (ParameterizedType) node;
			superClass = getTypeName(type.getType());
		}
		
		return superClass;
	}
}
