//-----------------------------------------------------------------------------
// $Id$
// $Source$
//-----------------------------------------------------------------------------

package gui;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import utils.*;

//-----------------------------------------------------------------------------

class AnalyzeCommand
{
    public static final int BWBOARD = 0;

    public static final int CBOARD = 1;

    public static final int DBOARD = 2;

    public static final int HSTRING = 3;

    public static final int HPSTRING = 4;

    public static final int NONE = 5;

    public static final int PLIST = 6;

    public static final int PSTRING = 7;

    public static final int PSPAIRS = 8;

    public static final int STRING = 9;

    public static final int SBOARD = 10;

    public static final int VAR = 11;

    public static final int VARB = 12;

    public static final int VARC = 13;

    public static final int VARP = 14;

    public static final int VARPO = 15;

    public static final int VARW = 16;

    public AnalyzeCommand(String line)
    {
        m_scale = 1.0;
        m_title = null;
        m_type = AnalyzeCommand.NONE;
        String array[] = line.split("/");
        String typeStr = array[0];        
        if (typeStr.equals("bwboard"))
            m_type = AnalyzeCommand.BWBOARD;
        else if (typeStr.equals("cboard"))
            m_type = AnalyzeCommand.CBOARD;
        else if (typeStr.equals("dboard"))
            m_type = AnalyzeCommand.DBOARD;
        else if (typeStr.equals("hstring"))
            m_type = AnalyzeCommand.HSTRING;
        else if (typeStr.equals("hpstring"))
            m_type = AnalyzeCommand.HPSTRING;
        else if (typeStr.equals("plist"))
            m_type = AnalyzeCommand.PLIST;
        else if (typeStr.equals("pspairs"))
            m_type = AnalyzeCommand.PSPAIRS;
        else if (typeStr.equals("pstring"))
            m_type = AnalyzeCommand.PSTRING;
        else if (typeStr.equals("string"))
            m_type = AnalyzeCommand.STRING;
        else if (typeStr.equals("sboard"))
            m_type = AnalyzeCommand.SBOARD;
        else if (typeStr.equals("var"))
            m_type = AnalyzeCommand.VAR;
        else if (typeStr.equals("varb"))
            m_type = AnalyzeCommand.VARB;
        else if (typeStr.equals("varc"))
            m_type = AnalyzeCommand.VARC;
        else if (typeStr.equals("varp"))
            m_type = AnalyzeCommand.VARP;
        else if (typeStr.equals("varpo"))
            m_type = AnalyzeCommand.VARPO;
        else if (typeStr.equals("varw"))
            m_type = AnalyzeCommand.VARW;
        m_label = array[1];
        m_command = array[2];
        if (array.length > 3)
            m_title = array[3];
        if (array.length > 4)
            m_scale = Double.parseDouble(array[4]);
    }

    public AnalyzeCommand(int type, String label, String command, String title,
                          double scale)
    {
        m_type = type;
        m_label = label;
        m_command = command;
        m_title = title;
        m_scale = scale;
    }

    public static AnalyzeCommand get(Frame owner, String label)
    {
        Vector commands = new Vector(128, 128);
        Vector labels = new Vector(128, 128);
        try
        {
            read(commands, labels, null);
        }
        catch (Exception e)
        {            
            SimpleDialogs.showError(owner, e.getMessage());
        }
        int index = labels.indexOf(label);
        if (index < 0)
            return null;
        return new AnalyzeCommand((String)commands.get(index));
    }

    public String getLabel()
    {
        return m_label;
    }

    public go.Color getColorArg()
    {
        return m_colorArg;
    }

    public go.Point getPointArg()
    {
        return m_pointArg;
    }

    public Vector getPointListArg()
    {
        return m_pointListArg;
    }

    public double getScale()
    {
        return m_scale;
    }

    public String getTitle()
    {
        return m_title;
    }

    public int getType()
    {
        return m_type;
    }

    public String getResultTitle()
    {
        StringBuffer buffer = new StringBuffer(m_label);
        if (needsColorArg() && m_colorArg != null)
        {
            if (m_colorArg == go.Color.BLACK)
                buffer.append(" Black");
            else
            {
                assert(m_colorArg == go.Color.WHITE);
                buffer.append(" White");
            }
        }
        if (needsPointArg() && m_pointArg != null)
        {
            buffer.append(' ');
            buffer.append(m_pointArg.toString());
        }
        else if (needsPointListArg())
        {
            for (int i = 0; i < m_pointListArg.size(); ++i)
            {
                buffer.append(' ');
                buffer.append(((go.Point)(m_pointListArg.get(i))).toString());
            }
        }
        if (needsStringArg() && m_stringArg != null)
        {
            buffer.append(' ');
            buffer.append(m_stringArg);
        }
        return buffer.toString();
    }

    public boolean isPointArgMissing()
    {
        if (needsPointArg())
            return (m_pointArg == null);
        if (needsPointListArg())
            return m_pointListArg.isEmpty();
        return false;
    }

    public boolean needsColorArg()
    {
        return needsColorArg(m_command);
    }

    public static boolean needsColorArg(String command)
    {
        return (command.indexOf("%c") >= 0);
    }

    public boolean needsFileArg()
    {
        return (m_command.indexOf("%f") >= 0);
    }

    public boolean needsPointArg()
    {
        return (m_command.indexOf("%p") >= 0);
    }

    public boolean needsPointListArg()
    {
        return (m_command.indexOf("%P") >= 0);
    }

    public boolean needsStringArg()
    {
        return (m_command.indexOf("%s") >= 0);
    }

    public static void read(Vector commands, Vector labels,
                            Vector supportedCommands)
        throws Exception
    {
        commands.clear();
        labels.clear();
        Vector files = getFiles();
        File file = new File(getDir(), "analyze-commands");
        if (! files.contains(file))
        {
            copyDefaults(file);
            files = getFiles();
        }
        for (int i = 0; i < files.size(); ++i)
            readFile((File)files.get(i), commands, labels, supportedCommands);
    }

    public String replaceWildCards(go.Color toMove, go.Color color)
    {
        if (needsColorArg())
            setColorArg(color);
        String result = m_command.replaceAll("%m", toMove.toString());
        if (needsPointArg())
            result = result.replaceAll("%p", m_pointArg.toString());
        if (needsPointListArg())
        {
            StringBuffer listBuffer = new StringBuffer(128);
            for (int i = 0; i < m_pointListArg.size(); ++i)
            {
                if (listBuffer.length() > 0)
                    listBuffer.append(' ');
                go.Point point = (go.Point)m_pointListArg.get(i);
                listBuffer.append(point.toString());
            }
            result = result.replaceAll("%P", listBuffer.toString());
        }
        if (needsFileArg())
        {
            result = result.replaceAll("%f", m_fileArg.toString());
        }
        if (needsStringArg())
        {
            assert(m_stringArg != null);
            result = result.replaceAll("%s", m_stringArg);
        }
        if (needsColorArg())
        {
            result = result.replaceAll("%c", m_colorArg.toString());
        }
        return result;
    }

    public void setColorArg(go.Color color)
    {
        assert(needsColorArg());
        m_colorArg = color;
    }

    public void setFileArg(File file)
    {
        assert(needsFileArg());
        m_fileArg = file;
    }

    public void setPointArg(go.Point point)
    {
        m_pointArg = point;
    }

    public void setStringArg(String value)
    {
        assert(needsStringArg());
        m_stringArg = value;
    }

    private int m_type;

    private double m_scale;

    private go.Color m_colorArg;

    private File m_fileArg;

    private String m_label;

    private String m_command;

    private String m_title;

    private String m_stringArg;

    private go.Point m_pointArg;

    private Vector m_pointListArg = new Vector();

    private static void copyDefaults(File file)
    {
        String resource = "config/analyze-commands";
        URL url = ClassLoader.getSystemClassLoader().getResource(resource);
        if (url == null)
            return;
        try
        {
            InputStream in = url.openStream();
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) >= 0)
                out.write(buffer, 0, n);
            in.close();
            out.close();
        }
        catch (Exception e)
        {
        }
    }

    private static File getDir()
    {
        String home = System.getProperty("user.home");
        return new File(home, ".gogui");
    }

    private static Vector getFiles()
    {
        Vector result = new Vector();
        File[] files = getDir().listFiles();
        if (files == null)
            return result;
        String s = new File(getDir(), "analyze-commands").toString();
        for (int i = 0; i < files.length; ++i)
        {
            File f = files[i];
            if (f.toString().startsWith(s) && ! f.toString().endsWith("~"))
                result.add(f);
        }
        return result;
    }

    public static void readFile(File file, Vector commands, Vector labels,
                                Vector supportedCommands)
        throws Exception
    {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String line;
        int lineNumber = 0;
        while ((line = in.readLine()) != null)
        {
            ++lineNumber;
            line = line.trim();
            if (line.length() > 0 && line.charAt(0) != '#')
            {
                String array[] = line.split("/");
                if (array.length < 3 || array.length > 5)
                    throw new Exception("Error in " + file + " line "
                                        + lineNumber);
                if (supportedCommands != null)
                {
                    String[] cmdArray
                        = StringUtils.tokenize(array[2].trim());
                    if (cmdArray.length == 0
                        || ! supportedCommands.contains(cmdArray[0]))
                        continue;
                }
                String label = array[1];
                if (labels.contains(label))
                    continue;
                labels.add(label);
                commands.add(line);
            }                
        }
        in.close();
    }
}

//-----------------------------------------------------------------------------
