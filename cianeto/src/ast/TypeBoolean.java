/*******************************************************************************
 * Integrantes:
 *  - Breno Viana de Oliveira
 *  - Michael dos Santos
 *******************************************************************************/
package ast;

public class TypeBoolean extends Type {

   public TypeBoolean() { super("boolean"); }

   @Override
   public String getCname() {
      return "int";
   }

}
