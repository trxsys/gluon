/* This file is part of Gluon.
 *
 * Gluon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gluon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gluon.  If not, see <http://www.gnu.org/licenses/>.
 */

package gluon;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import soot.*;
import soot.jimple.spark.SparkTransformer;
import soot.options.Options;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main
{
    private static final String PROGNAME="gluon";

    private static String classPath=null;
    private static String mainClassName=null;
    private static String moduleClassName=null;
    private static String contract=null;

    public static boolean WITH_JAVA_LIB=false;
    public static boolean TIME=false;
    public static boolean PROFILING_VARS=false;
    public static boolean CLASS_SCOPE=false;
    public static boolean ATOMICITY_SYNCH=false;
    public static boolean CONSERVATIVE_POINTS_TO=false;
    public static int TIMEOUT=0; /* timeout in seconds */

    public static void fatal(String error)
    {
        System.err.println(PROGNAME+": "+error);
        System.exit(-1);
    }

    public static void warning(String warning)
    {
        System.err.println(PROGNAME+": "+warning);
    }

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
		System.out.println("  -o, --contract <contract>       "
                           +"Module's contract (overrides annotation)");
		System.out.println("  -s, --class-scope               "
                           +"Restrict analysis to each class");
		System.out.println("  -y, --synch                     "
                           +"Atomicity is based on java synchronized");
        System.out.println("  -r, --conservative-points-to    "
                           +"Conservative points-to analysis.");
        System.out.println("                                  "
                           +"Use when there are classes loaded dynamically,");
        System.out.println("                                  "
                           +"which makes the regular points-to analysis");
        System.out.println("                                  "
                           +"incomplete");
        System.out.println("  -i, --timeout                   "
                           +"Timeout in mins for class scope analysis.");
		System.out.println("  -h, --help                      "
                           +"Display this help and exit");
    }

    private static void parseArguments(String[] args)
    {
        LongOpt[] options=new LongOpt[11];
        Getopt g;
        int c;

        options[0]=new LongOpt("help",LongOpt.NO_ARGUMENT,null,'h');
        options[1]=new LongOpt("classpath",LongOpt.REQUIRED_ARGUMENT,null,'c');
        options[2]=new LongOpt("module",LongOpt.REQUIRED_ARGUMENT,null,'m');
        options[3]=new LongOpt("with-java-lib",LongOpt.NO_ARGUMENT, null,'j');
        options[4]=new LongOpt("time",LongOpt.NO_ARGUMENT,null,'t');
        options[5]=new LongOpt("prof-vars",LongOpt.NO_ARGUMENT,null,'p');
        options[6]=new LongOpt("contract",LongOpt.REQUIRED_ARGUMENT, null,'o');
        options[7]=new LongOpt("class-scope",LongOpt.NO_ARGUMENT,null,'s');
        options[8]=new LongOpt("synch",LongOpt.NO_ARGUMENT,null,'y');
        options[9]=new LongOpt("conservative-points-to",LongOpt.NO_ARGUMENT,
                                null,'r');
        options[10]=new LongOpt("timeout",LongOpt.REQUIRED_ARGUMENT,null,'i');

        g=new Getopt(PROGNAME,args,"hc:m:o:jtpsyri",options);

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
            case 'o':
                {
                    contract=g.getOptarg();
                    break;
                }
            case 's':
                {
                    CLASS_SCOPE=true;
                    break;
                }
            case 'y':
                {
                    ATOMICITY_SYNCH=true;
                    break;
                }
            case 'r':
                {
                    CONSERVATIVE_POINTS_TO=true;
                    break;
                }
            case 'i':
                {
                    String timeout=null;

                    try
                    {
                        timeout=g.getOptarg();
                        TIMEOUT=Integer.parseInt(timeout)*60;
                    }
                    catch (NumberFormatException e)
                    {
                        fatal(timeout+": invalid format");
                    }

                    break;
                }
            case '?':
                {
                    /* getopt already printed an error */
                    System.exit(-1);
                    break;
                }
            }

        if (CONSERVATIVE_POINTS_TO && !CLASS_SCOPE)
            fatal("conservative points-to only makes sense with class scope analysis");

        if (TIMEOUT < 0)
            fatal("negative timeout");

        if (g.getOptind() != args.length-1)
            fatal("there must be one main class specified");

        mainClassName=args[g.getOptind()];
    }

    private static Map<String,String> getSparkOptions()
    {
        Map<String,String> opt = new HashMap<String,String>();

        opt.put("verbose","false");
        opt.put("propagator","worklist");
        opt.put("simple-edges-bidirectional","false");
        opt.put("on-fly-cg","true");
        opt.put("set-impl","double");
        opt.put("double-set-old","hybrid");
        opt.put("double-set-new","hybrid");

        return opt;
    }

    private static void run() throws IOException {
        Transform t;

        gluon.profiling.Timer.start("soot-init");
        Options.v().set_output_dir(Files.createTempDirectory("gluon").toString());
        Options.v().set_whole_program(true);
        Options.v().set_whole_shimple(true);

        Options.v().set_verbose(false);
        Options.v().set_interactive_mode(false);
        Options.v().set_debug(false);
        Options.v().set_debug_resolver(false);
        Options.v().set_show_exception_dests(false);

        Options.v().set_output_format(Options.output_format_none);

        Options.v().set_allow_phantom_refs(true);
        Options.v().set_keep_line_number(true);

        /* For line number information */
        PhaseOptions.v().setPhaseOption("tag.ln","on");

        /* Soot bug workaround */
        // PhaseOptions.v().setPhaseOption("jb","use-original-names:true");
        PhaseOptions.v().setPhaseOption("jb.ulp","enabled:false");

        /* For points-to analysis */
        PhaseOptions.v().setPhaseOption("cg.spark","enabled:true");

        if (false)
            Scene.v().setSootClassPath(classPath);
        else
            Scene.v().setSootClassPath(System.getProperty("java.class.path")
                                       +java.io.File.pathSeparator
                                       +classPath);

        t=new Transform("wstp.gluon",AnalysisMain.instance());

        AnalysisMain.instance().setModuleToAnalyze(moduleClassName);

        if (contract != null)
            AnalysisMain.instance().setContract(contract);

        PackManager.v().getPack("wstp").add(t);

        SootClass mainClass=Scene.v().loadClassAndSupport(mainClassName);

        Scene.v().addBasicClass(mainClassName,SootClass.SIGNATURES);
        Scene.v().addBasicClass(moduleClassName,SootClass.SIGNATURES);
        Scene.v().loadNecessaryClasses();
        Scene.v().loadDynamicClasses();

        try { Scene.v().setMainClass(mainClass); }
        catch (Exception e) { fatal("error loading main class"); }

        if (CLASS_SCOPE)
        {
            for (String path: classPath.split(":"))
                try
                {
                    Scanner sc=new Scanner(new File(path+"/LOADEXTRA"));

                    while (sc.hasNext())
                    {
                        String c=sc.next();

                        try { Scene.v().loadClassAndSupport(c); }
                        catch (Exception e) {};
                    }
                }
                catch (Exception e) {}

            for (SootClass c: Scene.v().getClasses())
                for (soot.SootMethod m: c.getMethods())
                    try { m.retrieveActiveBody(); }
                    catch (Exception e) {}

            for (SootClass c: Scene.v().dynamicClasses())
                for (soot.SootMethod m: c.getMethods())
                    try { m.retrieveActiveBody(); }
                    catch (Exception e) {}
        }

        /* Points-to analises */
        SparkTransformer.v().transform("",getSparkOptions());

        PackManager.v().runPacks();
    }

    private static void dumpRunTimes()
    {
        System.out.println();
        System.out.println("Run Time:");

        for (String id: gluon.profiling.Timer.getIds())
            System.out.printf("  %40s  %6d.%03ds\n",id,
                              gluon.profiling.Timer.getTime(id)/1000,
                              gluon.profiling.Timer.getTime(id)%1000);
    }

    private static void dumpProfilingVars()
    {
        System.out.println();
        System.out.println("Profiling Vars:");

        for (String id: gluon.profiling.Profiling.getIds())
            System.out.printf("  %40s  %9d\n",id,
                              gluon.profiling.Profiling.get(id));
    }

    public static void main(String[] args)
            throws Exception
    {
        if (args.length == 0)
        {
            help();
            System.exit(-1);
            return;
        }

        parseArguments(args);

        if (classPath == null)
            fatal("No classpath specified!");

        if (moduleClassName == null)
            fatal("No module specified!");

        gluon.profiling.Timer.start("total");
        run();
        gluon.profiling.Timer.stop("total");

        if (TIME)
            dumpRunTimes();

        if (PROFILING_VARS)
            dumpProfilingVars();
    }
}

/*
 * WIP!
 *
 *   * add install step to README
 *     * increase default max heap
 *   * rebase other branch
 *   * run validation test
 *   * write email w/ instructions
 */
