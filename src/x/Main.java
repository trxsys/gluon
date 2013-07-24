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
    public static boolean TIME=false;
    public static boolean PROFILING_VARS=false;
    public static boolean NO_GRAMMAR_OPTIMIZE=false;
    
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
		System.out.println("  -t, --time                      "
                           +"Output information about several run times");
		System.out.println("  -p, --prof-vars                 "
                           +"Output profiling variables");
		System.out.println("  -n, --no-grammar-opt            "
                           +"Disable grammar optimization");
		System.out.println("  -h, --help                      "
                           +"Display this help and exit");
    }
    
    private static void parseArguments(String[] args)
    {
        LongOpt[] options = new LongOpt[7];
        
        options[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        options[1] = new LongOpt("classpath", LongOpt.REQUIRED_ARGUMENT,
                                 null, 'c');
        options[2] = new LongOpt("module", LongOpt.REQUIRED_ARGUMENT, 
                                 null, 'm');
        options[3] = new LongOpt("with-java-lib", LongOpt.NO_ARGUMENT, 
                                 null, 'j');
        options[4] = new LongOpt("time", LongOpt.NO_ARGUMENT, null, 't');
        options[5] = new LongOpt("prof-vars", LongOpt.NO_ARGUMENT, null, 'p');
        options[6] = new LongOpt("no-grammar-opt", LongOpt.NO_ARGUMENT, null, 'n');

        Getopt g = new Getopt(PROGNAME, args, "hc:m:jtpn", options);
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
            case 't': 
                {
                    TIME=true;
                    break;
                }
            case 'p': 
                {
                    PROFILING_VARS=true;
                    break;
                }
            case 'n': 
                {
                    NO_GRAMMAR_OPTIMIZE=true;
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

        x.profiling.Timer.start("final:soot-init");

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
    
    private static void dumpRunTimes()
    {
        System.out.println();
        System.out.println("Run Time:");

        for (String id: x.profiling.Timer.getIds())
            if (id.startsWith("final:"))
                System.out.printf("  %40s  %5d.%03ds\n",id.replace("final:",""),
                                  x.profiling.Timer.getTime(id)/1000,
                                  x.profiling.Timer.getTime(id)%1000);
    }

    private static void dumpProfilingVars()
    {
        System.out.println();
        System.out.println("Profiling Vars:");

        for (String id: x.profiling.Profiling.getIds())
            if (id.startsWith("final:"))
                System.out.printf("  %40s  %5d\n",id.replace("final:",""),
                                  x.profiling.Profiling.get(id));
    }

    public static void main(String[] args)
    {
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

        x.profiling.Timer.start("final:total");
        run();
        x.profiling.Timer.stop("final:total");

        if (TIME)
            dumpRunTimes();

        if (PROFILING_VARS)
            dumpProfilingVars();
    }
}
