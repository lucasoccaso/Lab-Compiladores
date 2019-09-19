// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package ast;

public class TypeNull extends Type {

	public TypeNull() {
		super("NullType");
	}

	@Override
	public String getCname() {
		return "NULL";
	}

}
