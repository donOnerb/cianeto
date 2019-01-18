package ast;

import java.util.*;	
/*
 * Krakatoa Class
 */
public class CianetoClass extends Type {

   public CianetoClass( String name ) {
      super(name);
   }

   @Override
   public String getCname() {
      return getName();
   }

   private String name;
   private CianetoClass superclass;
   private ArrayList<Field> fieldList;
   private ArrayList<Method> publicMethodList, privateMethodList;
   // métodos públicos get e set para obter e iniciar as variáveis acima,
   // entre outros métodos
}
