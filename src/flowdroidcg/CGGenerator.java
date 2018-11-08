package flowdroidcg;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
45656
public class CGGenerator {
    //设置android的jar包目录
    public final static String jarPath = "/home/xueling/Android/Sdk/platforms/";
    //设置要分析的APK文件
    public final static String apkpath = "/home/xueling/apkAnalysis/APK/";
    public final static  String CGpath = "/home/xueling/apkAnalysis/callGraph/";
    
    private static Map<String,Boolean> visited = new HashMap<String,Boolean>();      
    public static void main(String[] args){    
    		    
    //	genCallGraph("/home/xueling/apkAnalysis/APK/test/com.holidaycheck.apk","com.holidaycheck.apk");
   // }
  
    		  //get the callGrag existing files
            	File callGraphfileList[] = new File(CGpath).listFiles();
            	String callGraphNameList[] = new String[callGraphfileList.length];
            	for(int i=0;i < callGraphfileList.length; i++) { 
            		callGraphNameList[i] = callGraphfileList[i].getName();
            	}            	
                       		    
              //generate callGraph 	
        	  File fileList[] = new File(apkpath).listFiles();
        	  System.out.println(fileList.length);
    		  String apkNameList[] = new String[fileList.length];
    		  for (int i=0;i < fileList.length; i++) {
    		    	apkNameList[i] = fileList[i].getName();  		    
    		    if(Arrays.asList(callGraphNameList).contains(apkNameList[i]+".gexf")){
    	    		    	System.out.println(apkNameList[i] + " exists !!");
    	    		        continue;
    	    		     }	
    	    		    
    	    	try { 	 
    		   		genCallGraph(apkpath+apkNameList[i],apkNameList[i]);    		
    	
    		  }catch(NullPointerException ex ) {
    		    		System.out.println(apkNameList[i] + " NullPointerException !!");
    		    		continue;
    		    	}     
    		    	catch(RuntimeException ex ) {
    		    		System.out.println(apkNameList[i] + " RuntimeException !!");
    		    		continue;
    		    	} 
    		  }
    }
    		   
//  private static void genCallGraph(String apk, String apkName) throws NullPointerException, RuntimeException {
	private static void genCallGraph(String apk, String apkName) throws NullPointerException, RuntimeException{
        SetupApplication app = new SetupApplication(jarPath, apk);     
        soot.G.reset();
        
        app.setCallbackFile(CGGenerator.class.getResource("AndroidCallback.txt").getFile());
        app.constructCallgraph();
        
        SootMethod entryPoint = app.getDummyMainMethod(); 

        //获取函数调用图
        CallGraph cg = Scene.v().getCallGraph();

        //可视化函数调用图
        CGExporter cge = new CGExporter();
        visit(cg,entryPoint,cge);      
  
        //导出函数调用图
      // cge.exportMIG(apkName, CGpath_batch);       
         cge.exportMIG(apkName, CGpath);
      
    }
    //可视化函数调用图的函数
   // private static void visit(CallGraph cg,SootMethod m){
        private static void visit(CallGraph cg,SootMethod m, CGExporter cge){
        //在soot中，函数的signature就是由该函数的类名，函数名，参数类型，以及返回值类型组成的字符串
        String identifier = m.getSignature();
        //记录是否已经处理过该点
        visited.put(m.getSignature(), true);
        //以函数的signature为label在图中添加该节点
        cge.createNode(m.getSignature());
        //获取调用该函数的函数
        Iterator<MethodOrMethodContext> ptargets = new Targets(cg.edgesInto(m));
        if(ptargets != null){
            while(ptargets.hasNext())
            {
                SootMethod p = (SootMethod) ptargets.next();
                if(p == null){
                    System.out.println("p is null");
                }
                if(!visited.containsKey(p.getSignature())){
                //	visit(cg,p);
                    visit(cg,p,cge);
                }
            }
        }
        //获取该函数调用的函数
        Iterator<MethodOrMethodContext> ctargets = new Targets(cg.edgesOutOf(m));
        if(ctargets != null){
            while(ctargets.hasNext())
            {
                SootMethod c = (SootMethod) ctargets.next();
                if(c == null){
                    System.out.println("c is null");
                }
                //将被调用的函数加入图中
                cge.createNode(c.getSignature());
                //添加一条指向该被调函数的边
                cge.linkNodeByID(identifier, c.getSignature());
                if(!visited.containsKey(c.getSignature())){
                    //递归
                 //   visit(cg,c);
                    visit(cg,c,cge);
                }
            }
        }
    }
}


