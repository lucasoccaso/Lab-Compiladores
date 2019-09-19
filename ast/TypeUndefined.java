// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package ast;

public class TypeUndefined extends Type {
    // variables that are not declared have this type
    
   public TypeUndefined() { super("undefined"); }
   
   public String getCname() {
      return "int";
   }
   
}
