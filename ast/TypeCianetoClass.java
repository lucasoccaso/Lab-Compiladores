// Nome: Iago Freitas Piedade      RA: 587567
// Nome: Lucas Alexandre Occaso    RA: 620505

package ast;
/*
 * Krakatoa Class
 */
public class TypeCianetoClass extends Type {

   public TypeCianetoClass( String name ) {
      super(name);
   }

   @Override
   public String getCname() {
      return getName();
   }

   private String name;
   private TypeCianetoClass superclass;
   // private FieldList fieldList;
   // private MethodList publicMethodList, privateMethodList;
   // métodos públicos get e set para obter e iniciar as variáveis acima,
   // entre outros métodos
}
