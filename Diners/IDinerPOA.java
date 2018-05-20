package Diners;


/**
* Diners/IDinerPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Diners.idl
* Wednesday, March 30, 2016 12:21:49 AM EDT
*/

public abstract class IDinerPOA extends org.omg.PortableServer.Servant
 implements Diners.IDinerOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("requestFromRight", new java.lang.Integer (0));
    _methods.put ("requestFromLeft", new java.lang.Integer (1));
    _methods.put ("forkFromRight", new java.lang.Integer (2));
    _methods.put ("forkFromLeft", new java.lang.Integer (3));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {

  // put method specifications here
       case 0:  // Diners/IDiner/requestFromRight
       {
         this.requestFromRight ();
         out = $rh.createReply();
         break;
       }

       case 1:  // Diners/IDiner/requestFromLeft
       {
         this.requestFromLeft ();
         out = $rh.createReply();
         break;
       }

       case 2:  // Diners/IDiner/forkFromRight
       {
         this.forkFromRight ();
         out = $rh.createReply();
         break;
       }

       case 3:  // Diners/IDiner/forkFromLeft
       {
         this.forkFromLeft ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:Diners/IDiner:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public IDiner _this() 
  {
    return IDinerHelper.narrow(
    super._this_object());
  }

  public IDiner _this(org.omg.CORBA.ORB orb) 
  {
    return IDinerHelper.narrow(
    super._this_object(orb));
  }


} // class IDinerPOA