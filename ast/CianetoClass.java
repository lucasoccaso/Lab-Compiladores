package ast;

import java.util.Hashtable;

public class CianetoClass {
   private String name;
   private CianetoClass superClass;
   private Hashtable<String, Object> cianetoMethods;
   private Hashtable<String, Object> cianetoAttributes;


   public CianetoClass() {
      this.cianetoMethods = new Hashtable<String, Object>();
      this.cianetoAttributes = new Hashtable<String, Object>();
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public CianetoClass getSuperClass() {
      return superClass;
   }

   public void setSuperClass(CianetoClass superClass) {
      this.superClass = superClass;
   }

   public void putAttribute(String key, Object value) {
      this.cianetoAttributes.put(key, value);
   }

   public CianetoAttribute getAttribute(String key) {
      return (CianetoAttribute) this.cianetoAttributes.get(key);
   }

   public void putMethod(String key, Object value) {
      this.cianetoMethods.put(key, value);
   }

   public CianetoMethod getMethod(String key) {
      return (CianetoMethod) this.cianetoMethods.get(key);
   }
}