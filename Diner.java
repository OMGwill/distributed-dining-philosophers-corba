//Will Luttmann
//Homework4
import Diners.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;


class RandomSleep {
	static public void SleepAWhile(int min) 	{
		try 		{
			Thread.sleep(min + (int)(Math.random() * 1000));
		}
		catch (InterruptedException e) 		{
			e.printStackTrace();
		}
	}
}

class SharedFork {
	private boolean wantedByNeighbor = false;
	private boolean requested = false;
	private boolean mine = false;

	public SharedFork(boolean isMine) {
		wantedByNeighbor = false;
		requested = false;
		mine = isMine;
	}
	public synchronized 
	boolean isWantedByNeighbor() {
		return (wantedByNeighbor);
	}
	public synchronized 
	boolean wasRequested() {
		return (requested);
	}
	public synchronized 
	boolean isMine() {
		return (mine);
	}
	public synchronized 
	boolean isReleasable() {
		return (mine && wantedByNeighbor);
	}
	public synchronized 
	boolean isRequestable() {
		return (!mine && !requested);
	}
	public synchronized 
	void setWantedByNeighbor() {
		wantedByNeighbor = true;
	}
	public synchronized 
	void setRequested() {
		requested = true;
	}
	public synchronized 
	void release() {
		wantedByNeighbor = false;
		mine = false;
	}
	public synchronized 
	void receive() {
		requested = false;
		mine = true;
	}
}


 class Diner 
{
	public static enum DinerState {THINKING, HUNGRY, EATING};
	public DinerState myState;
	public SharedFork leftFork, rightFork;
	public String myName, leftNeighborName, rightNeighborName;
	public boolean initialized = false;
	public static IDiner dinerImpl;
	public static IDiner leftNeighbor, rightNeighbor;

	protected Diner (String myn, boolean hasleft, String leftneighborn, 
			boolean hasright, String rightneighborn) {
		System.out.format("%s got args %b, %s, %b, %s\n", 
				myn, hasleft, leftneighborn, hasright, rightneighborn);
		initialized = false;
		myState = DinerState.THINKING;
		myName = myn;
		leftFork = new SharedFork(hasleft);
		leftNeighborName = leftneighborn;
		rightFork = new SharedFork(hasright);
		rightNeighborName = rightneighborn;
		System.out.print("initial state: ");
		showState();

		if (leftNeighbor != null && rightNeighbor != null)
			initialized = true;
		System.out.print("Initial state: ");
		showState();
	}

	// Diner state-handling methods
	protected  
	void sendMyRightFork()
	 {
		showState();
		System.out.format("%s sending right fork to %s.\n", myName, rightNeighborName);
        System.out.println ("sendMyRightFork(): " + Thread.currentThread().getName() + 
                " priority: " + Thread.currentThread().getPriority());
        while (!initialized) {
        	System.out.println ("...wait until both rightNeighbor and leftNeighbor are set...");
        	RandomSleep.SleepAWhile(100);
        }
        synchronized(rightFork) {
		rightFork.release();
		rightNeighbor.forkFromLeft();
        }
		System.out.format("Finished sending right fork to %s.\n", rightNeighborName);
		showState();
	}
	protected  
	void sendMyLeftFork()
	 {
		showState();
		System.out.format("%s sending left fork to %s.\n", myName, leftNeighborName);
        System.out.println ("sendMyLeftFork(): " + Thread.currentThread().getName() + 
                " priority: " + Thread.currentThread().getPriority());
        while (!initialized) {
        	System.out.println ("...wait until both rightNeighbor and leftNeighbor are set...");
        	RandomSleep.SleepAWhile(100);
        }
        synchronized(leftFork) {
		leftFork.release();
		leftNeighbor.forkFromRight();
        }
		System.out.format("Finished sending left fork to %s.\n", leftNeighborName);
		showState();
	}	
	protected 
	void requestMyRightFork()
	 {
		showState();
		System.out.format("%s requesting right fork from %s.\n", myName, rightNeighborName);
        System.out.println ("requestMyRightFork(): " + Thread.currentThread().getName() + 
                " priority: " + Thread.currentThread().getPriority());
        while (!initialized) {
        	System.out.println ("...wait until both rightNeighbor and leftNeighbor are set...");
        	RandomSleep.SleepAWhile(100);
        }
		rightFork.setRequested();
		rightNeighbor.requestFromLeft(); // request right fork
		System.out.format("Finished requesting right fork from %s.\n", rightNeighborName);
		showState();
	}
	protected 
	void requestMyLeftFork()
	 {
		showState();
		System.out.format("%s requesting left fork from %s.\n", myName, leftNeighborName);
        System.out.println ("requestMyLeftFork(): " + Thread.currentThread().getName() + 
                " priority: " + Thread.currentThread().getPriority());
        while (!initialized) {
        	System.out.println ("...wait until both rightNeighbor and leftNeighbor are set...");
        	RandomSleep.SleepAWhile(100);
        }
		leftFork.setRequested();
		leftNeighbor.requestFromRight();  // request left fork
		System.out.format("Finished requesting left fork from %s.\n", leftNeighborName);
		showState();
	}	
	protected void showState() {
		try {
			if (leftFork.isWantedByNeighbor()) {
				System.out.format("%s ", leftNeighborName);
				System.out.print("wants left fork; ");
			}
			System.out.format("%s ", myName);
			if (leftFork.isMine()) {
				System.out.print("has left fork, ");
			}
			if (leftFork.wasRequested())
				System.out.print("has requested left fork, ");
			System.out.print("is ");
			System.out.print(myState);
			if (rightFork.isMine()) {
				System.out.print(", has right fork");
			}
			if (rightFork.wasRequested())
				System.out.print(", has requested right fork");
			System.out.print("; ");
			if (rightFork.isWantedByNeighbor()) {
				System.out.format("%s ", rightNeighborName);
				System.out.print("wants right fork");
			}
		}
		catch (Exception e) {
			System.err.format("Exception from showState for %s",myName);
		}
		System.out.println();
	}
	protected void tryToSendForks()
	 {
		// pre: !EATING
		if (myState == DinerState.EATING)
			return;
		if (myState == DinerState.THINKING && rightFork.isReleasable())
			sendMyRightFork();
		if (myState == DinerState.THINKING && leftFork.isReleasable())
			sendMyLeftFork();
		if (myState == DinerState.HUNGRY && leftFork.isReleasable() && !rightFork.isMine())
			sendMyLeftFork();
	}
	protected void tryToRequestForks() 
	 {
		// pre: HUNGRY
		if (myState != DinerState.HUNGRY)
			return;
		if (rightFork.isRequestable()) 
			requestMyRightFork();
		if (rightFork.isMine() && leftFork.isRequestable()) 
			requestMyLeftFork();	
	}
	protected void tryToEat() 
	 {
		// pre: HUNGRY
		// post: HUNGRY || THINKING
		//System.out.println("Entering tryToEat().");
		//showState();
		if (myState != DinerState.HUNGRY)
			return;
		tryToSendForks();
		tryToRequestForks();
		RandomSleep.SleepAWhile(1);
		if (rightFork.isMine() && leftFork.isMine())
			eat(); // changes state to THINKING
		tryToSendForks();
		//showState();
		//System.out.println("Exiting tryToEat().");
	}
	protected void eat() {
		// pre: HUNGRY
		// post: THINKING
//		System.out.println("Entering eat().");
//        System.out.println ("eat(): " + Thread.currentThread().getName() + 
//                " priority: " + Thread.currentThread().getPriority());
		myState = DinerState.EATING;
//		showState();
		System.out.format("\t\t\t\t\t%s is EATING!\n", myName);
		RandomSleep.SleepAWhile(1000); // EATING
		// now done eating
		myState = DinerState.THINKING;
//		showState();
//		System.out.println("Exiting eat().");
	}
	protected void think() {
		// pre: THINKING
		// post: HUNGRY
//		System.out.println("Entering think().");
//        System.out.println ("think(): " + Thread.currentThread().getName() + 
//                " priority: " + Thread.currentThread().getPriority());
		System.out.format("\t%s is thinking...\n", myName);
		RandomSleep.SleepAWhile(1000); // THINKING
		myState = DinerState.HUNGRY;
//		showState();
//		System.out.format("\t\t\t%s is hungry...\n", myName);
//		System.out.println("Exiting think().");
	}
	protected void run()
	 {
		while (true) {
			if (myState == DinerState.THINKING)
				think(); // changes state to HUNGRY
			if (myState == DinerState.HUNGRY)
				tryToEat(); // returns with state HUNGRY || THINKING
		}
	}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			  private class IDinerImpl extends IDinerPOA
			{	
				 private ORB orb;

				public void setORB(ORB orb_val) {
					orb = orb_val;
				}
			
				public //synchronized 
				void requestFromRight()
				 {
					System.out.format("%s got request for right fork from %s.\n", myName, rightNeighborName);
					System.out.println ("requestFromRight(): " + Thread.currentThread().getName() + 
							" priority: " + Thread.currentThread().getPriority());
					rightFork.setWantedByNeighbor();
					//showState();
					if (myState == Diner.DinerState.THINKING && rightFork.isMine())
						sendMyRightFork();
					System.out.format("Finished handling right fork request from %s.\n", rightNeighborName);
					showState();
				}
				public //synchronized 
				void requestFromLeft()
				 {
					System.out.format("%s got request for left fork from %s.\n", myName, leftNeighborName);
					System.out.println ("requestFromLeft(): " + Thread.currentThread().getName() + 
							" priority: " + Thread.currentThread().getPriority());

					leftFork.setWantedByNeighbor();
					showState();
					if (myState == Diner.DinerState.THINKING && leftFork.isMine()) 
						sendMyLeftFork();
					System.out.format("Finished handling left fork request from %s.\n", leftNeighborName);
					showState();
				}	
				public //synchronized 
				void forkFromRight() {
					System.out.format("%s received right fork from %s.\n", myName, rightNeighborName);
					System.out.println ("forkFromRight(): " + Thread.currentThread().getName() + 
							" priority: " + Thread.currentThread().getPriority());
					rightFork.receive();
					System.out.format("Finished receiving right fork from %s.\n", rightNeighborName);
					showState();
				}
				public //synchronized 
				void forkFromLeft() {
					System.out.format("%s received left fork from %s.\n", myName, leftNeighborName);
					System.out.println ("forkFromLeft(): " + Thread.currentThread().getName() + 
							" priority: " + Thread.currentThread().getPriority());
					leftFork.receive();
					System.out.format("Finished receiving left fork from %s.\n", leftNeighborName);
					showState();
				}
			}
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
	public static void main(String[] args) {
    	String myName, leftNeighborName, rightNeighborName;
    	boolean hasLeft, hasRight;
		
    	if (args.length != 5) {
    		System.err.println("Sorry-- need 5 command line args");
    		// ToDo: print description of cmd line args
    		System.exit(0);
    	}
		
    	System.out.format("cmd line args are: %s %s %s %s %s\n", 
    		args[0], args[1], args[2], args[3], args[4]);
    	myName = args[0];
    	hasLeft = args[1].equals("true");
    	leftNeighborName = args[2];
    	hasRight = args[3].equals("true");
    	rightNeighborName = args[4];
    	
    	Diner d = new Diner(myName, hasLeft, leftNeighborName, 
    			 hasRight, rightNeighborName);


    	try {
    	
			String[] corbaArgs = {"-ORBInitialPort", "1050"};
			
			//create and init orb
			ORB orb = ORB.init(corbaArgs, null);
			
			// register the component with the Naming Service
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			
			//activate components
			rootpoa.the_POAManager().activate();
			IDinerImpl compImpl = d.new IDinerImpl();
			compImpl.setORB(orb);
			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(compImpl);

			IDiner compRef = IDinerHelper.narrow(ref);  // got the component reference
			
			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");

			// Use NamingContextExt instead of NamingContext. This is 
			// part of the Interoperable naming Service.  
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// bind the Object Reference in Naming
			 NameComponent path[] = ncRef.to_name(myName);
			 ncRef.rebind(path, compRef);

			// resolve the Object Reference in Naming
			dinerImpl = IDinerHelper.narrow(ncRef.resolve_str(myName));
			System.out.println("Obtained a handle on server object: " + myName);
			
			RandomSleep.SleepAWhile(1000);
			
			leftNeighbor = IDinerHelper.narrow(ncRef.resolve_str(leftNeighborName));
			System.out.println("Obtained a handle on server object: " + leftNeighborName);
			RandomSleep.SleepAWhile(1000);
			
			
			rightNeighbor = IDinerHelper.narrow(ncRef.resolve_str(rightNeighborName));
			System.out.println("Obtained a handle on server object: " + rightNeighborName);
			RandomSleep.SleepAWhile(1000);
			
			//everyone is initialized!!!
			d.initialized = true;

			//now that we are all set, we can think, get hungry, and EAT!
			d.run();
			
    	} 
    	catch (Exception e) {
    		System.err.format("%s exception while running: %s", 
    				myName, e.getMessage());
    		//System.exit(0);
    		e.printStackTrace();
    	}	
	}
}
