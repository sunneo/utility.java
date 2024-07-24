package com.example.sharp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.example.events.EventDelegate;
import com.example.events.INotification;
import com.example.events.INotificationEventArgs;
import com.example.events.WritableValue;
import com.example.sharp.coroutine.AsyncTask;
import com.example.sharp.io.MemoryStream;

public class FileCommunicator{
    public String outputDir;
    public String inputDir;
    protected String name="FileComm<Unnamed>";
    protected EventDelegate.Event2<String,String> callbacks = EventDelegate.create(String.class,String.class);
    protected WritableValue<Boolean> running = new WritableValue<>();
    protected WatchService watcher;
    protected boolean removeOnRead=true;
    static class FileCommunicatorOutputStream extends OutputStream{
    	protected FileCommunicator owner;
    	protected MemoryStream mStream = new MemoryStream();
    	public FileCommunicatorOutputStream(FileCommunicator owner) {
    		this.owner = owner;
    	}
		@Override
		public void write(int b) throws IOException {
			mStream.writeByte((byte)b);
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			mStream.write(b, off,len);
		}
		@Override
		public void flush() throws IOException {
			mStream.setPosition(0);
			byte[] bytes = mStream.readAllBytes();
			String content = new String(bytes);
			owner.write(content);
			mStream.setCapacity(0);
		}
		@Override
		public void close() throws IOException {
			super.close();
		}
		
    	
    }
    static class FileCommunicatorInputStream extends InputStream{
    	protected MemoryStream mStream = new MemoryStream();
    	protected volatile long writePos = 0;
    	protected volatile long readPos = 0;
    	protected FileCommunicator owner;
    	EventDelegate.Event2<String,String> router;
    	protected INotification<INotificationEventArgs.INotificationEventArg2<String,String>> callbackStreaming = (s,e)->{
    		byte[] bytes = e.get_1().getBytes();
			mStream.setPosition(writePos);
			mStream.write(bytes, 0, bytes.length);
			writePos = mStream.getPosition();
    	};
    	protected void waitForInput(int len) {
    		while(readPos + len >= writePos) {
				try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
    	}
    	protected void waitForInput() {
    		waitForInput(0);
    	}
    	protected void rewindIfNeed() {
    		if(readPos >= writePos) {
    			readPos = 0;
    			writePos = 0;
    		}
    	}
    	public FileCommunicatorInputStream(FileCommunicator owner) {
    		this.owner = owner;
    		router = this.owner.callbacks.route2();
    		router.addDelegate(callbackStreaming);
    	}
    	
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			waitForInput(len);
			long currentPos = mStream.getPosition();
			mStream.setPosition(readPos);
			int ret = mStream.read(b,off,len);
			readPos = mStream.getPosition();
			mStream.setPosition(currentPos);
			rewindIfNeed();
			return ret;
		}
		

		@Override
		public int read() throws IOException {
			waitForInput();
			long currentPos = mStream.getPosition();
			mStream.setPosition(readPos);
			int ret = mStream.read();
			readPos = mStream.getPosition();
			mStream.setPosition(currentPos);
			rewindIfNeed();
			return ret;
		}

		@Override
		public byte[] readAllBytes() throws IOException {
			return super.readAllBytes();
		}

		@Override
		public byte[] readNBytes(int len) throws IOException {
			waitForInput(len);
			long currentPos = mStream.getPosition();
			mStream.setPosition(readPos);
			byte[] ret = mStream.readNBytes(len);
			readPos = mStream.getPosition();
			mStream.setPosition(currentPos);
			rewindIfNeed();
			return ret;
		}

		@Override
		public int readNBytes(byte[] b, int off, int len) throws IOException {
			waitForInput(len);
			long currentPos = mStream.getPosition();
			mStream.setPosition(readPos);
			int ret = mStream.readNBytes(b,off,len);
			readPos = mStream.getPosition();
			mStream.setPosition(currentPos);
			rewindIfNeed();
			return ret;
		}

		@Override
		public void close() throws IOException {
			if(router != null) {
				router.dispose();
				router = null;
			}
			super.close();
			
		}
		
    }
    public Writer getAsWriter() {
    	return new OutputStreamWriter(new FileCommunicatorOutputStream(this));
    }
    /**
     * make file communicator as Reader
     * @return
     */
    public Reader getAsReader() {
    	return new InputStreamReader(new FileCommunicatorInputStream(this));
    }
    public void setRemoveOnRead(boolean b) {
        removeOnRead=b;
    }
    public FileCommunicator(String outDir,String inDir) {
        this(outDir,inDir,"");
    }
    public FileCommunicator(String outDir,String inDir, String name) {
        this.setOutputDir(outDir);
        this.setInputDir(inDir);
        this.name = name;
    }
    public void setOnFileInputed(Delegates.Action2<String,String> callback) {
    	if(callback == null) return;
    	callbacks.addDelegate((s,e)->{
    		callback.Invoke(e.get_1(), e.get_2());
    	});
    }
    public String write(String content, String path) {
        try (FileWriter writer=new FileWriter(path, Charset.defaultCharset())){
            writer.write(content);
            writer.close();
        }catch(Exception ee) {
            Tracer.D(ee);
        }
        return path;
    }
    public String write(String content) {
        long threadId=0;
        try {
           threadId=Thread.currentThread().getId();
        }catch(Exception ee) {
            Tracer.D(ee);
        }
        String path = Paths.get(outputDir, "t"+Long.toString(threadId)).toString();
        return write(content, path);
    }
    public void setOutputDir(String outputDir) {
        if(!new File(outputDir).exists()) {
        	new File(outputDir).mkdirs();
        }
        this.outputDir=outputDir;
    }
    public void setInputDir(String inputDir) {
    	 if(!new File(inputDir).exists()) {
         	new File(inputDir).mkdirs();
         }
        this.inputDir=inputDir;
    }
    public void start() {
        // if previous one available, just set to false
        running.set(false);
        running = new WritableValue<Boolean>();
        try {
            running.set(true);
            WritableValue<Boolean> localRunning=running;
            watcher = FileSystems.getDefault().newWatchService();
            AsyncTask task = new AsyncTask(()->onPoll(watcher,localRunning));
            task.SetName(name);
            task.FlushJob(false);
        } catch (java.nio.file.ClosedWatchServiceException ex) {
            // keep it silent
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void stop() {
        running.set(false);
        running = new WritableValue<Boolean>();
        try {
            if(watcher != null) {
               watcher.close();
            }
        }catch(Exception ee) {
            Tracer.D(ee);
        }
    }
    protected void onExceptionOccurred(Exception ee) {
        ee.printStackTrace();
        stop();
    }
    volatile boolean shouldTrigger=true;
    @SuppressWarnings("unchecked")
    protected void onPoll(WatchService watcher, WritableValue<Boolean> running) {
        WatchKey key = null;
        java.nio.file.Path dir = FileSystems.getDefault().getPath(inputDir);
        try {
            key = dir.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (Exception ee) {
            Tracer.D(ee);
            onExceptionOccurred(ee);
        }
        while(running.get()) {
            try {
                key = watcher.take();
                for (WatchEvent<?> event: key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    
                    if(!shouldTrigger) {
                        continue;
                    }
                    // The filename is the
                    // context of the event.
                    WatchEvent<java.nio.file.Path> ev = (WatchEvent<java.nio.file.Path>)event;
                    java.nio.file.Path filename = ev.context();

                    // Verify that the new
                    //  file is a text file.
                    try {
                        // Resolve the filename against the directory.
                        // If the filename is "test" and the directory is "foo",
                        // the resolved name is "test/foo".
                        java.nio.file.Path child = dir.resolve(filename);
                        String contentType = Files.probeContentType(child);
                        if (!CString.IsNullOrEmpty(contentType) && !"text/plain".contentEquals(contentType)) {
                            System.err.format("New file '%s'" +
                                " is not a plain text file.%n", filename);
                            continue;
                        }
                        if(callbacks!=null) {
                            String absPath=child.toFile().getAbsolutePath();
                            if(new File(absPath).exists()) {
                            	String content = null;
                            	try(BufferedReader reader=new BufferedReader(new FileReader(absPath))){
                            		StringBuilder strb = new StringBuilder();
                            		while(true) {
                            			String line = reader.readLine();
                            			if(line == null) break;
                            			strb.append(line+"\n");
                            		}
                            		content=strb.toString();
                            	} catch(Exception ee) {
                            		Tracer.D(ee);
                            	}
                                if(content != null) {
                                	callbacks.invoke(this, content,absPath );
                                }
                                if (!CString.IsNullOrEmpty(content)) {
                                    if(removeOnRead) {
                                        shouldTrigger=false;
                                        new File(absPath).delete();
                                        shouldTrigger=true;
                                    }
                                }                                   
                            }
                        }
                    } catch (IOException x) {
                        System.err.println(x);
                        continue;
                    }

                }

                // Reset the key -- this step is critical if you want to
                // receive further watch events.  If the key is no longer valid,
                // the directory is inaccessible so exit the loop.
                boolean valid = key.reset();
                if (!valid) {
                    break;
                } 
            }catch(Exception ee) {
                Tracer.D("["+String.valueOf(name)+"]"+"FileCommunicator Stopped because "+ String.valueOf(ee.getMessage()),true);
                onExceptionOccurred(ee);
            }
        }
        key.cancel();
    }
}