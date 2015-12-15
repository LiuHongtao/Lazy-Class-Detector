package visitor;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

public class AssociateSetDetector extends ASTVisitor{
	
	private Set<String> associateClassSet = new HashSet<String>();
	
	public Set<String> getAssociateClassSet() {
		return associateClassSet;
	}
	
	public AssociateSetDetector(FieldDeclaration[] decList) {
		for (FieldDeclaration dec : decList) {
			dec.accept(this);
		}
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		//if it associated with other
		String typeName = getTypeName(
				node.getType());
		if (typeName != null) {
			associateClassSet.add(typeName);
		}
		
		return true;
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
