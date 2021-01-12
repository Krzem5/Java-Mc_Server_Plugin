package com.krzem.mc_server_plugin;



import java.io.File;
import java.io.FileWriter;
import java.lang.Exception;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class WorldCommand extends Command{
	public static final List<String> MAIN_FUNCITIONS=WorldCommand.list("create","list","tp","load","unload","delete");
	public static final List<String> CREATE_FUNCITIONS=WorldCommand.list("void","superflat","default","buffet");
	public static final List<String> WORLD_PRESETS=WorldCommand.list("classic_flat","tunnellers_dream","water_world","overworld","snowy_kingdom","bottomless_pit","desert","redstone_ready","void","custom","json");
	public static final Map<String,String> WORLD_PRESETS_CODE=WorldCommand.map("classic_flat","	minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;minecraft:plains;village","tunnellers_dream","minecraft:bedrock,230*minecraft:stone,5*minecraft:dirt,minecraft:grass_block;minecraft:mountains;stronghold,biome_1,decoration,dungeon,mineshaft","water_world","minecraft:bedrock,5*minecraft:stone,5*minecraft:dirt,5*minecraft:sand,90*minecraft:water;minecraft:deep_ocean;biome_1,oceanmonument","overworld","minecraft:bedrock,59*minecraft:stone,3*minecraft:dirt,minecraft:grass_block;minecraft:plains;stronghold,biome_1,village,decoration,dungeon,lake,mineshaft,lava_lake","snowy_kingdom","minecraft:bedrock,59*minecraft:stone,3*minecraft:dirt,minecraft:grass_block,minecraft:snow;minecraft:snowy_tundra;village,biome_1","bottomless_pit","2*minecraft:cobblestone,3*minecraft:dirt,minecraft:grass_block;minecraft:plains;biome_1,village","desert","minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone,8*minecraft:sand;minecraft:desert;stronghold,biome_1,village,decoration,dungeon,mineshaft","redstone_ready","minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone;","void","minecraft:air;minecraft:the_void;decoration");
	public static final List<String> BUFFET_GENERATION_TYPES=WorldCommand.list("surface","caves","floating_islands","json");
	public static final List<String> BUFFET_GENERATION_TERRAINS=WorldCommand.list("snowy_tundra","ice_spikes","snowy_taiga","snowy_taiga_mountain","frozen_river","snowy_beach","mountains","gravelly_mountains","wooded_mountains","gravelly_mountains+","taiga","taiga_mountains","giant_tree_taiga","giant_spruce_taiga","stone_shore","plains","sunflower_plains","forest","flower_forest","birch_forest","tall_birch_forest","dark_forest","dark_forest_hills","swamp","swamp_hills","jungle","modified_jungle","modified_jungle_edge","bamboo_jungle","river","mushroom_fields","mushroom_fields_shore","end","small_end_islands","end_midlands","end_highlands","end_barrens","desert","desert_lake","savanna","shattered_savanna","badlands","eroded_badlands","wooded_badlands_plateau","modified_wooded_badlands_plateau","savanna_plateau","badlands_plateau","modified_savanna_plateau","modified_badlands_plateau","nether","warm_ocean","lukewarm_ocean","deep_lukewarm_ocean","ocean","cold_ocean","deep_ocean","deep_cold_ocean","frozen_ocean","deep_frozen_ocean","void","wooded_hills","taiga_hills","snowy_taiga_hills","jungle_hills","desert_hills","birch_forest_hills","tall_birch_hills","giant_tree_taiga_hills","giant_spruce_taiga_hills","snowy_mountains");
	private Main cls;
	private String _bwn;
	private Map<String,Map<String,Location>> _pp=new HashMap<String,Map<String,Location>>();
	private Map<String,String> _lw=WorldCommand.map();



	public WorldCommand(Main cls){
		super("world","World save file manipulation","/world",new ArrayList<String>());
		this.cls=cls;
		this.setPermission("bukkit.command.op");
		WorldCommand wcls=this;
		this.cls.getServer().getPluginManager().registerEvents(new Listener(){
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent e){
				wcls._load_player(e.getPlayer());
			}



			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent e){
				wcls._save_player(e.getPlayer());
			}
		},this.cls);
		this._bwn=this.cls.getServer().getWorlds().get(0).getName();
		this.load();
		try{
			Field c_map_f=this.cls.getServer().getClass().getDeclaredField("commandMap");
			c_map_f.setAccessible(true);
			CommandMap c_map=(CommandMap)c_map_f.get(this.cls.getServer());
			c_map.register("world",this);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}



	@Override
	public boolean execute(CommandSender cs,String lb,String[] args){
		if (args.length==1&&args[0].equals("list")){
			String s="Loaded Worlds: [";
			for (String k:this._world_list()){
				s+=ChatColor.GREEN+k+ChatColor.RESET+", ";
			}
			s=s.substring(0,s.length()-2)+"], Unloaded Worlds: [";
			boolean a=false;
			for (String k:this._load_world_list()){
				if (k.equals("*")){
					continue;
				}
				s+=ChatColor.GREEN+k+ChatColor.RESET+", ";
				a=true;
			}
			cs.sendMessage((a==false?s:s.substring(0,s.length()-2))+"]");
			return true;
		}
		if (args.length==2&&args[0].equals("load")){
			if (args[1].equals("*")){
				for (String wn:this._load_world_list()){
					if (wn.equals("*")){
						continue;
					}
					this.cls.getServer().createWorld(new WorldCreator(wn));
				}
			}
			else{
				if (this._load_world_list().contains(args[1])){
					this.cls.getServer().createWorld(new WorldCreator(args[1]));
				}
			}
			return true;
		}
		if (args.length==2&&args[0].equals("unload")){
			if (args[1].equals("*")){
				for (String wn:this._unload_world_list()){
					if (wn.equals("*")){
						continue;
					}
					for (Player p:this.cls.getServer().getWorld(wn).getPlayers()){
						this._move_to_default(p);
					}
					this.cls.getServer().unloadWorld(wn,true);
				}
			}
			else{
				if (this._unload_world_list().contains(args[1])){
					for (Player p:this.cls.getServer().getWorld(args[1]).getPlayers()){
						this._move_to_default(p);
					}
					this.cls.getServer().unloadWorld(args[1],true);
				}
			}
			return true;
		}
		if (args.length==2&&args[0].equals("delete")){
			if (this._delete_world_list().contains(args[1])){
				if (this._world_list().contains(args[1])){
					for (Player p:this.cls.getServer().getWorld(args[1]).getPlayers()){
						this._move_to_default(p);
					}
					for (Map.Entry<String,String> e:this._lw.entrySet()){
						if (e.getValue().equals(args[1])){
							this._lw.put(e.getKey(),this._bwn);
						}
					}
					this._pp.remove(args[1]);
					this.cls.getServer().unloadWorld(args[1],true);
				}
				this._del(new File(String.format("%s/%s/",this.cls.getServer().getWorldContainer().getAbsolutePath(),args[1])));
			}
			return true;
		}
		if (args.length==1&&args[0].equals("tp")){
			this._move_to_default((Player)cs);
			return true;
		}
		if (args.length==2&&args[0].equals("tp")){
			if (this._world_list().contains(args[1])){
				this._move_player((Player)cs,args[1]);
			}
			return true;
		}
		if (args.length>=3&&args[0].equals("tp")){
			if (this._world_list().contains(args[1])){
				for (int i=2;i<args.length;i++){
					for (Player p:this.cls.getServer().getOnlinePlayers()){
						if (p.getDisplayName().equals(args[i])){
							this._move_player(p,args[1]);
						}
					}
				}
			}
			return true;
		}
		if (args.length==3&&args[0].equals("create")&&args[2].equals("default")){
			if (!this._all_worlds().contains(args[1])){
				this.cls.getServer().createWorld(new WorldCreator(args[1]));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==5&&args[0].equals("create")&&args[2].equals("default")&&args[3].equals("seed")){
			if (!this._all_worlds().contains(args[1])){
				this.cls.getServer().createWorld(new WorldCreator(args[1]).seed(Long.parseLong(args[4])));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==3&&args[0].equals("create")&&args[2].equals("void")){
			if (!this._all_worlds().contains(args[1])){
				WorldCreator c=new WorldCreator(args[1]);
				c.generator(new ChunkGenerator(){
					public ChunkGenerator.ChunkData generateChunkData(World w,Random r,int ox,int oz,ChunkGenerator.BiomeGrid bg){
						ChunkData dt=this.createChunkData(w);
						for(int x=0;x<16;x++){
							for(int z=0;z<16;z++){
								bg.setBiome(x,z,Biome.THE_VOID);
							}
						}
						return dt;
					}



					public Location getFixedSpawnLocation(World w,Random r){
						w.getBlockAt(0,99,0).setType(Material.BARRIER);
						return new Location(w,0,100,0);
					}
				});
				this.cls.getServer().createWorld(c);
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==4&&args[0].equals("create")&&args[2].equals("superflat")){
			if (!this._all_worlds().contains(args[1])){
				String c=this._superflat(this.WORLD_PRESETS_CODE.get(args[3]));
				this.cls.getServer().createWorld(new WorldCreator(args[1]).type(WorldType.FLAT).generatorSettings(c));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==5&&args[0].equals("create")&&args[2].equals("superflat")&&args[3].equals("json")){
			if (!this._all_worlds().contains(args[1])){
				this.cls.getServer().createWorld(new WorldCreator(args[1]).type(WorldType.FLAT).generatorSettings(args[4]));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==5&&args[0].equals("create")&&args[2].equals("superflat")&&args[3].equals("custom")){
			if (!this._all_worlds().contains(args[1])){
				this.cls.getServer().createWorld(new WorldCreator(args[1]).type(WorldType.FLAT).generatorSettings(this._superflat(args[4])));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==5&&args[0].equals("create")&&args[2].equals("buffet")&&args[3].equals("json")){
			if (!this._all_worlds().contains(args[1])){
				this.cls.getServer().createWorld(new WorldCreator(args[1]).type(WorldType.BUFFET).generatorSettings(args[4]));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		if (args.length==5&&args[0].equals("create")&&args[2].equals("buffet")){
			if (!this._all_worlds().contains(args[1])){
				this.cls.getServer().createWorld(new WorldCreator(args[1]).type(WorldType.BUFFET).generatorSettings(this._buffet(args[3],args[4])));
				this.cls.getServer().getWorld(args[1]).save();
			}
			return true;
		}
		return false;
	}



	public List<String> tabComplete(CommandSender s,String a,String[] args){
		if (args.length==1){
			return StringUtil.copyPartialMatches(args[0],MAIN_FUNCITIONS,this._list());
		}
		else if (args.length==3&&args[0].equals("create")){
			return StringUtil.copyPartialMatches(args[2],CREATE_FUNCITIONS,this._list());
		}
		else if (args.length==4&&args[0].equals("create")&&args[2].equals("superflat")){
			return StringUtil.copyPartialMatches(args[3],WORLD_PRESETS,this._list());
		}
		else if (args.length==4&&args[0].equals("create")&&args[2].equals("buffet")){
			return StringUtil.copyPartialMatches(args[3],BUFFET_GENERATION_TYPES,this._list());
		}
		else if (args.length==5&&args[0].equals("create")&&args[2].equals("buffet")&&!args[3].equals("json")){
			return StringUtil.copyPartialMatches(args[4],BUFFET_GENERATION_TERRAINS,this._list());
		}
		else if (args.length==4&&args[0].equals("create")&&args[2].equals("default")){
			return StringUtil.copyPartialMatches(args[3],this._list("seed"),this._list());
		}
		else if (args.length==2&&args[0].equals("tp")){
			return StringUtil.copyPartialMatches(args[1],this._world_list(),this._list());
		}
		else if (args.length>=3&&args[0].equals("tp")){
			return super.tabComplete(s,a,args);
		}
		else if (args.length==2&&args[0].equals("load")){
			return StringUtil.copyPartialMatches(args[1],this._load_world_list(),this._list());
		}
		else if (args.length==2&&args[0].equals("unload")){
			return StringUtil.copyPartialMatches(args[1],this._unload_world_list(),this._list());
		}
		else if (args.length==2&&args[0].equals("delete")){
			return StringUtil.copyPartialMatches(args[1],this._delete_world_list(),this._list());
		}
		return this._list();
	}



	public void load(){
		for (World w:this.cls.getServer().getWorlds()){
			if (!w.equals(this._bwn)){
				this.cls.getServer().unloadWorld(w.getName(),true);
			}
		}
		File d=this.cls.getDataFolder();
		if (new File(d,"data.xml").exists()){
			try{
				Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(d,"data.xml").getAbsolutePath());
				doc.getDocumentElement().normalize();
				Element root=doc.getDocumentElement();
				Element pl_e=this._xml_child(root,"players").get(0);
				for (Element p_e:this._xml_child(pl_e,"player")){
					this._lw.put(p_e.getAttribute("name"),p_e.getAttribute("last-world"));
					this.cls.getServer().createWorld(new WorldCreator(p_e.getAttribute("last-world")));
					for (Element l_e:this._xml_child(p_e,"location")){
						if (this._pp.get(l_e.getAttribute("world"))==null){
							this._pp.put(l_e.getAttribute("world"),new HashMap<String,Location>());
						}
						World w=new WorldCreator(l_e.getAttribute("world")).createWorld();
						this._pp.get(l_e.getAttribute("world")).put(p_e.getAttribute("name"),new Location(w,Double.parseDouble(l_e.getAttribute("x")),Double.parseDouble(l_e.getAttribute("y")),Double.parseDouble(l_e.getAttribute("z")),Float.parseFloat(l_e.getAttribute("ry")),Float.parseFloat(l_e.getAttribute("rx"))));
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}



	public void save(){
		File d=this.cls.getDataFolder();
		if (!d.exists()){
			d.mkdir();
		}
		try{
			for (Player p:this.cls.getServer().getOnlinePlayers()){
				this._lw.put(p.getDisplayName(),p.getWorld().getName());
				if (this._pp.get(p.getWorld().getName())==null){
					this._pp.put(p.getWorld().getName(),new HashMap<String,Location>());
				}
				this._pp.get(p.getWorld().getName()).put(p.getDisplayName(),p.getLocation().clone());
			}
			Document doc=DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element root=doc.createElement("plugin");
			Element pl_e=doc.createElement("players");
			for (Map.Entry<String,String> e:this._lw.entrySet()){
				Element p_e=doc.createElement("player");
				p_e.setAttribute("name",e.getKey());
				p_e.setAttribute("last-world",e.getValue());
				for (Map.Entry<String,Map<String,Location>> we:this._pp.entrySet()){
					try{
						Location l=we.getValue().get(e.getKey());
						Element l_e=doc.createElement("location");
						l_e.setAttribute("world",we.getKey());
						l_e.setAttribute("x",Double.toString(l.getX()));
						l_e.setAttribute("y",Double.toString(l.getY()));
						l_e.setAttribute("z",Double.toString(l.getZ()));
						l_e.setAttribute("rx",Float.toString(l.getPitch()));
						l_e.setAttribute("ry",Float.toString(l.getYaw()));
						p_e.appendChild(l_e);
					}
					catch (Exception ex){

					}
				}
				pl_e.appendChild(p_e);
			}
			root.appendChild(pl_e);
			doc.appendChild(root);
			StreamResult o=new StreamResult(new FileWriter(new File(d.getAbsolutePath(),"data.xml")));
			Transformer t=TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT,"yes");
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","4");
			t.setOutputProperty(OutputKeys.VERSION,"1.0");
			t.setOutputProperty(OutputKeys.ENCODING,"utf-8");
			t.transform(new DOMSource(doc),o);
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}



	private void _load_player(Player p){
		if (this._lw.get(p.getDisplayName())!=null){
			p.teleport(this._pp.get(this._lw.get(p.getDisplayName())).get(p.getDisplayName()));
		}
		this._save_player(p);
	}



	private void _save_player(Player p){
		this._lw.put(p.getDisplayName(),p.getWorld().getName());
		if (this._pp.get(p.getWorld().getName())==null){
			this._pp.put(p.getWorld().getName(),new HashMap<String,Location>());
		}
		this._pp.get(p.getWorld().getName()).put(p.getDisplayName(),p.getLocation().clone());
	}



	private void _move_to_default(Player p){
		this._move_player(p,this._bwn);
	}



	private void _move_player(Player p,String wn){
		World w=this.cls.getServer().getWorld(wn);
		if (this._pp.get(p.getWorld().getName())==null){
			this._pp.put(p.getWorld().getName(),new HashMap<String,Location>());
		}
		if (this._pp.get(wn)==null){
			this._pp.put(wn,new HashMap<String,Location>());
		}
		if (this._pp.get(wn).get(p.getDisplayName())==null){
			this._pp.get(wn).put(p.getDisplayName(),w.getSpawnLocation().clone());
		}
		this._pp.get(p.getWorld().getName()).put(p.getDisplayName(),p.getLocation().clone());
		Location l=this._pp.get(wn).get(p.getDisplayName()).clone();
		l.setWorld(w);
		p.teleport(l);
		this._lw.put(p.getDisplayName(),wn);
	}



	private List<String> _world_list(){
		List<String> o=new ArrayList<String>();
		for (World w:this.cls.getServer().getWorlds()){
			o.add(w.getName());
		}
		return o;
	}



	private List<String> _unload_world_list(){
		List<String> o=new ArrayList<String>();
		o.add("*");
		for (World w:this.cls.getServer().getWorlds()){
			if (this._bwn.equals(w.getName())){
				continue;
			}
			o.add(w.getName());
		}
		return o;
	}



	private List<String> _load_world_list(){
		List<String> o=new ArrayList<String>();
		o.add("*");
		List<String> ll=this._world_list();
		for (File f:this.cls.getServer().getWorldContainer().listFiles()){
			if (f.isDirectory()==false||f.getName().equals("plugins")||f.getName().equals("update")||f.getName().equals("logs")||!Pattern.compile("^[a-zA-Z0-9/._-]*$").matcher(f.getName()).matches()||ll.contains(f.getName())){
				continue;
			}
			o.add(f.getName());
		}
		return o;
	}



	private List<String> _delete_world_list(){
		List<String> o=new ArrayList<String>();
		for (File f:this.cls.getServer().getWorldContainer().listFiles()){
			if (f.isDirectory()==false||f.getName().equals("plugins")||f.getName().equals("update")||f.getName().equals("logs")||!Pattern.compile("^[a-z0-9/._-]*$").matcher(f.getName()).matches()||this._bwn.equals(f.getName())){
				continue;
			}
			o.add(f.getName());
		}
		return o;
	}



	private List<String> _all_worlds(){
		List<String> o=new ArrayList<String>();
		for (File f:this.cls.getServer().getWorldContainer().listFiles()){
			if (f.isDirectory()==false||f.getName().equals("plugins")||f.getName().equals("update")||f.getName().equals("logs")||!Pattern.compile("^[a-z0-9/._-]*$").matcher(f.getName()).matches()){
				continue;
			}
			o.add(f.getName());
		}
		return o;
	}



	private List<String> _list(String... l){
		List<String> o=new ArrayList<String>();
		for (String s:l){
			o.add(s);
		}
		return o;
	}



	private Map<String,String> _map(String... l){
		Map<String,String> o=new HashMap<String,String>();
		for (int i=0;i<l.length;i+=2){
			o.put(l[i],l[i+1]);
		}
		return o;
	}



	private void _del(File d){
		for(File f:d.listFiles()){
			if (f.isDirectory()){
				this._del(f);
			}
			else{
				f.delete();
			}
		}
		d.delete();
	}



	private String _superflat(String _c){
		String[] c=_c.split(";");
		String ls="";
		for (String s:c[0].split(",")){
			if (s.split("\\*").length==1){
				s="1*"+s;
			}
			ls+=String.format(",{\"block\":\"%s\",\"height\":%s}",s.split("\\*")[1],s.split("\\*")[0]);
		}
		ls=(ls.length()>0?ls.substring(1):ls);
		String ss="";
		for (String s:c[2].split(",")){
			String os="";
			if (s.split("\\(").length==2){
				for (String p:s.split("\\(")[1].replace(")","").split(" ")){
					os+=String.format(",\"%s\":%s",p.split("=")[0],p.split("=")[1]);
				}
			}
			ss+=String.format(",\"%s\":{%s}",s.split("\\(")[0],(os.length()>0?os.substring(1):os));
		}
		ss=(ss.length()>0?ss.substring(1):ss);
		return String.format("{\"structures\":{%s},\"layers\":[%s],\"biome\":\"%s\"}",ss,ls,c[1]);
	}



	private String _buffet(String t,String b){
		return String.format("{\"biome_source\":{\"options\":{\"biomes\":[\"%s\"]},\"type\":\"fixed\"},\"chunk_generator\":{\"options\":{\"default_block\":\"stone\",\"default_fluid\":\"water\"},\"type\":\"%s\"}}",b,t);
	}



	private ArrayList<Element> _xml_child(Element p,String tn){
		ArrayList<Element> o=new ArrayList<Element>();
		NodeList cl=p.getChildNodes();
		for (int j=0;j<cl.getLength();j++){
			if (cl.item(j).getNodeType()!=Node.ELEMENT_NODE){
				continue;
			}
			Element e=(Element)cl.item(j);
			if (e.getTagName().equals(tn)){
				o.add(e);
			}
		}
		return o;
	}



	private static List<String> list(String... l){
		List<String> o=new ArrayList<String>();
		for (String s:l){
			o.add(s);
		}
		return o;
	}



	private static Map<String,String> map(String... l){
		Map<String,String> o=new HashMap<String,String>();
		for (int i=0;i<l.length;i+=2){
			o.put(l[i],l[i+1]);
		}
		return o;
	}
}
