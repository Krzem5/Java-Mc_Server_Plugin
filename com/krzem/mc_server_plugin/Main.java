package com.krzem.mc_server_plugin;



import java.lang.Exception;
import java.lang.Runnable;
import java.lang.Thread;
import org.bukkit.plugin.java.JavaPlugin;



public class Main extends JavaPlugin{
	private WorldCommand wc=null;



	@Override
	public void onDisable(){
		this.wc.save();
	}



	@Override
	public void onEnable(){
		this.wc=new WorldCommand(this);
		Main cls=this;
		new Thread(new Runnable(){
			@Override
			public void run(){
				while (true){
					cls.wc.save();
					try{
						Thread.sleep(1000);
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
}
