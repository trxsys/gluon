package x;

import soot.PackManager;
import soot.Transform;

import soot.PhaseOptions;
import soot.options.Options;

import soot.Scene;
import soot.SootClass;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

public class Main
{
    private static final String PROGNAME="x";
    
    private static String classPath=null;
    private static String mainClassName=null;
    private static String moduleClassName=null;

    public static boolean WITH_JAVA_LIB=false;
    
    private static void help() 
    {
		System.out.println("Usage: "+PROGNAME+" OPTIONS <main_class>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -m, --module <module>           "
                           +"Module to verify");
		System.out.println("  -c, --classpath <class_path>    "
                           +"Java classpath");
		System.out.println("  -j, --with-java-lib             "
                           +"Do not refrain from analyzing java library code");
		System.out.println("  -h, --help                      "
                           +"Display this help and exit");
    }
    
    private static void parseArguments(String[] args)
    {
        LongOpt[] options = new LongOpt[4];
        
        options[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        options[1] = new LongOpt("classpath", LongOpt.REQUIRED_ARGUMENT,
                                 null, 'c');
        options[2] = new LongOpt("module", LongOpt.REQUIRED_ARGUMENT, 
                                 null, 'm');
        options[3] = new LongOpt("with-java-lib", LongOpt.NO_ARGUMENT, 
                                 null, 'j');
        
        Getopt g = new Getopt(PROGNAME, args, "hc:m:j", options);
        int c;
        
        g.setOpterr(true);
        
        while ((c = g.getopt()) != -1)
            switch (c)
            {
            case 'h': 
                {
                    help();
                    System.exit(0);
                    break;
                }
            case 'c': 
                {
                    classPath=g.getOptarg();
                    break;
                }
            case 'm': 
                {
                    moduleClassName=g.getOptarg();
                    break;
                }
            case 'j': 
                {
                    WITH_JAVA_LIB=true;
                    break;
                }
            }
        
        if (g.getOptind() != args.length-1)
        {
            System.err.println(PROGNAME+": there must be one "
                               +"main class specified");
            System.exit(-1);
        }
        
        mainClassName=args[g.getOptind()];
    }
    
    private static void run()
    {
        Transform t;
        
        Options.v().set_whole_program(true);
        Options.v().set_whole_shimple(true);
        
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);
        
        Options.v().set_verbose(false);
        Options.v().set_interactive_mode(false);
        Options.v().set_debug(false);
        Options.v().set_debug_resolver(false);
        Options.v().set_show_exception_dests(false);
        
        Options.v().set_keep_line_number(true);
        
        Options.v().set_output_format(Options.output_format_shimple);
        
        // line number information
        PhaseOptions.v().setPhaseOption("tag.ln","on");
        
        if (false)
            Scene.v().setSootClassPath(classPath);
        else
            Scene.v().setSootClassPath(System.getProperty("sun.boot.class.path")
                                       +java.io.File.pathSeparator
                                       +classPath);
        
        // PhaseOptions.v().setPhaseOption("jb", "use-original-names:true);
        PhaseOptions.v().setPhaseOption("jb.ulp", "enabled:false");
        t=new Transform("wstp.x",AnalysisMain.instance());
        
        AnalysisMain.instance().setModuleToAnalyze(moduleClassName);
        
        PackManager.v().getPack("wstp").add(t);
        
        SootClass c = Scene.v().loadClassAndSupport(mainClassName);
        
        Scene.v().addBasicClass(mainClassName,SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
        
        Scene.v().setMainClass(c);
        
        PackManager.v().runPacks();        
    }



    private static void test()
    {
        x.cfg.Cfg grammar=new x.cfg.Cfg();
        x.cfg.NonTerminal S=new x.analysis.programPattern.PPNonTerminal("S");
        x.cfg.NonTerminal A=new x.analysis.programPattern.PPNonTerminal("A");
        x.cfg.NonTerminal B=new x.analysis.programPattern.PPNonTerminal("B");
        x.cfg.NonTerminal C=new x.analysis.programPattern.PPNonTerminal("C");
        x.cfg.NonTerminal D=new x.analysis.programPattern.PPNonTerminal("D");
        x.cfg.NonTerminal E=new x.analysis.programPattern.PPNonTerminal("E");
        x.cfg.NonTerminal F=new x.analysis.programPattern.PPNonTerminal("F");
        x.cfg.Terminal a=new x.analysis.programPattern.PPTerminal("a");
        x.cfg.Terminal b=new x.analysis.programPattern.PPTerminal("b");
        x.cfg.Terminal c=new x.analysis.programPattern.PPTerminal("c");
        x.cfg.Terminal d=new x.analysis.programPattern.PPTerminal("d");
        x.cfg.Terminal e=new x.analysis.programPattern.PPTerminal("e");
        x.cfg.Terminal f=new x.analysis.programPattern.PPTerminal("f");
        x.cfg.Terminal g=new x.analysis.programPattern.PPTerminal("g");
        x.cfg.Terminal h=new x.analysis.programPattern.PPTerminal("h");
        x.cfg.Terminal i=new x.analysis.programPattern.PPTerminal("i");
        x.cfg.Terminal j=new x.analysis.programPattern.PPTerminal("j");
        x.cfg.Production start=new x.cfg.Production(S);

        start.appendToBody(a);

        grammar.addProduction(start);

        grammar.setStart(S);

        System.out.println(grammar.toString());

        System.out.println();
        System.out.println("-----------------------");
        System.out.println();

        grammar.subwordClosure();

        System.out.println();
        System.out.println("-----------------------");
        System.out.println();

        System.out.println(grammar.toString());
        
        System.exit(0);
    }

    
    public static void main(String[] args)
    {
        test();

        if (args.length == 0)
        {
            help();
            System.exit(-1);
            return;
        }
        
        parseArguments(args);
        
        if (classPath == null) 
        {
            System.err.println("No classpath specified!");
            System.exit(-1);
            return;
		}
        
        if (moduleClassName == null)
        {
            System.err.println("No module specified!");
            System.exit(-1);
            return;
        }
        
        run();
    }
}
