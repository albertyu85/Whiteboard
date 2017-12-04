import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ClientServer implements ModelListener{
	 private java.util.List<ObjectOutputStream> outputs = new ArrayList<ObjectOutputStream>();
	 private ClientHandler clientHandler;
	 private ServerAccepter serverAccepter;
	 private Canvas c;
	 public ClientServer(Canvas c)  {
		  this.c = c;
	  }
	    
	public void doServer() {
        String result = JOptionPane.showInputDialog("Run server on port", "8001");
        if (result!=null) {
            System.out.println("server: start");
            serverAccepter = new ServerAccepter(Integer.parseInt(result.trim()));
            serverAccepter.start();
        }
    }
    
    public void doClient() {
        String result = JOptionPane.showInputDialog("Connect to host:port", "127.0.0.1:8001");
        if (result!=null) {
            String[] parts = result.split(":");
            System.out.println("client: start");
            clientHandler = new ClientHandler(parts[0].trim(), Integer.parseInt(parts[1].trim()));
            clientHandler.start();
            
        }
    }
    
    public synchronized void sendRemote(Message message) {
        System.out.println("server: send " + message);
        // Convert the message object into an xml string.
        OutputStream memStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(memStream);
        encoder.writeObject(message);
        encoder.close();
        String xmlString = memStream.toString();
        // Now write that xml string to all the clients.
        Iterator<ObjectOutputStream> it = outputs.iterator();
        while (it.hasNext()) {
            ObjectOutputStream out = it.next();
            try {
                out.writeObject(xmlString);
                out.flush();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                it.remove();
            }
        }
    }
    
   
    
    public synchronized void addOutput(ObjectOutputStream out) {
        outputs.add(out);
        
    }
	
	
	public static class Message {
        public DShapeModel model;
		public String text;
        public Message() {
            text = null;
            model = null;
        }

        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }
        
        public DShapeModel getModel(){
        	return model;
        }

        public void setModel(DShapeModel model) {
            this.model = model;
        }
        
        public String toString() {
            return "message: " + text;
        }  
    }
	
	
	 private class ClientHandler extends Thread {
         private String name;
         private int port;
         ClientHandler(String name, int port) {
             this.name = name;
             this.port = port;
         }
     // Connect to the server, loop getting messages
         public void run() {
             try {
                 
                 Socket toServer = new Socket(name, port); // make connection to the server name/port
                 ObjectInputStream in = new ObjectInputStream(toServer.getInputStream()); // get input stream to read from server and wrap in object input stream
                 System.out.println("client: connected! ATTEMPTING");
                 while (true) {
                     String xmlString = (String) in.readObject();
                     XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(xmlString.getBytes()));
                     Message message = (Message) decoder.readObject();
                     invokeToGUI(message);
                     
                 }
             }
             catch (Exception ex) { // IOException and ClassNotFoundException
                ex.printStackTrace();
             }
        }
    } 
	
	 
	 
	 public void invokeToGUI(Message message) {
	        final Message temp = message;
	        SwingUtilities.invokeLater( new Runnable() {
	            public void run() {
	       
	            if("ADD".equals(temp.getText())){
	       			 c.addShape(temp.getModel(), false);  //CHANGE BACK TO TRUE
	       			System.out.print("adddddd");
	       		 }
	       		 else if("Deleted".equals(temp.getText())){
	       			 c.removeShape();
	       			 
	       		 }
	       		 
	       		 else if("MoveBack".equals(temp.getText())){
	       			 System.out.print("MoveBack");
	       			 c.moveToBack();
	       		 }
	       		 else if("MoveFront".equals(temp.getText())){
	       			System.out.print("MoveFront");
	       			 c.moveToFront();
	       			 
	       		 }
	       		 else if("Resize".equals(temp.getText())){
	       			 //dont know what to do yet
	       			c.getSelected().setWidth(temp.getModel().getWidth());
	       			c.getSelected().setHeight(temp.getModel().getHeight());
	       			
	       			if(temp.getModel() instanceof DLineModel){
	       				c.getSelected().setX(temp.getModel().getX());
	       				c.getSelected().setY(temp.getModel().getY());
	       				((DLineModel)c.getSelected().getModel()).setX2(((DLineModel)temp.getModel()).getX2());
	       				((DLineModel)c.getSelected().getModel()).setY2(((DLineModel)temp.getModel()).getY2());
	       			}
	       			System.out.print("RESIZE");
	       		 }
	       		 
	       		 else if("Moved".equals(temp.getText())){
	       			 //dont know what to do yet
	       			c.getSelected().setX(temp.getModel().getX());
	       			c.getSelected().setY(temp.getModel().getY());
	       			if(temp.getModel() instanceof DLineModel){
	       				((DLineModel)c.getSelected().getModel()).setX2(((DLineModel)temp.getModel()).getX2());
	       				((DLineModel)c.getSelected().getModel()).setY2(((DLineModel)temp.getModel()).getY2());
	       			}
	       			System.out.println("MOVEDDDDDDD");
	       		 }
	       		 
	       		 else if("Color".equals(temp.getText())){
	       			System.out.print("CHANGECOLOR");
	       			c.getSelected().setColor(temp.getModel().getColor());
	       		 }
	       		 
	       		 else if("FontChanged".equals(temp.getText())){
	       			System.out.print("Font changed");
	       			c.changeFont((((DTextModel)temp.getModel()).getFont())); 
	       			 //c.changeFont(newFont);  gonna be a problem
	       		}
	       			 
	       		 else if("TextChanged".equals(temp.getText())){
	       			System.out.print("text changed");	
	       			 c.changeString((((DTextModel)temp.getModel()).getText())); 
	       		}
	       		
	       		 else if("Selected".equals(temp.getText())){
	       			//System.out.print("I AM SELECTED");
	       			for(int i =0; i< c.list.size(); i++){
	       				if(c.list.get(i).getModel().equals(temp.getModel())){
	       					System.out.print("MODEL MATCHED");
	       					c.setSelected(c.list.get(i));
	       				}
	       			}
	    		 }
	            	
	            	//c.addShape(temp.getModel(), false);
	            }
	           
	        });
	        
	         
	    }
	     
	 
	 
	 
	 class ServerAccepter extends Thread {
	        private int port;
	        ServerAccepter(int port) {
	            this.port = port;
	        }
	        public void run() {
	            try {
	                ServerSocket serverSocket = new ServerSocket(port);
	                while (true) {
	                    Socket toClient = null;
	                    toClient = serverSocket.accept();
	                    System.out.println("server: got client");
	                    addOutput(new ObjectOutputStream(toClient.getOutputStream()));
	                }
	            } catch (IOException ex) {
	                ex.printStackTrace(); 
	            }
	        }
	    }

	
	 public void addShapes(DShapeModel model){
		 Message message = new Message();
		 message.setModel(model);
		 message.setText("ADD");
		 sendRemote(message);
		 //model.setAdd(false);
	 }
	 
	 public void sendAll(DShapeModel[] all){
		 Message[] messages = new Message[all.length];
		 for(int i = 0; i <messages.length; i++){
			 messages[i].setModel(all[i]);
			 messages[i].setText("ADD");
			 sendRemote(messages[i]);
		 }
		 
	 }
	
	public void modelChanged(DShapeModel model) {
		Message message = new Message();
		message.setModel(model);
		
		if(model.getAdd()){
			 message.setText("ADD");
			 model.setAdd(false);
		 }
		 else if(model.getDeleted()){
			 message.setText("Deleted");
			 model.setDeleted(false);
		 }
		 
		 else if(model.getMoveBack()){
			 message.setText("MoveBack");
			 model.setMoveBack(false);
		 }
		 else if(model.getMoveFront()){
			 message.setText("MoveFront");
			 model.setMoveFront(false);
		 }
		 else if(model.getResize()){
			 message.setText("Resize");
			 model.setResize(false);
		 }
		 
		 else if(model.getMoved()){
			 message.setText("Moved");
			 model.setMoved(false);
		 }
		 
		 else if(model.getColored()){
			 message.setText("Color");
			 model.setColored(false);
		 }
		
		 else if(model instanceof DTextModel){
			 if(((DTextModel)model).getFontChanged()){
				message.setText("FontChanged");
				((DTextModel)model).setFontChanged(false);
			 }
			 
			 else if(((DTextModel)model).getTextChanged()){
					message.setText("TextChanged");
					((DTextModel)model).setTextChanged(false);
				 }
		 }
		 
		 else if(model.isSelected()){
			 message.setText("Selected");
		 }
		 
		 sendRemote(message);
	}
	
	
	
}