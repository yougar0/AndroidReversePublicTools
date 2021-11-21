package jadx.gui.plugins.lazyjni;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.gui.ui.MainWindow;

import javax.swing.*;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LazyJniAction {

	private final transient MainWindow mainWindow;

	public LazyJniAction(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void start() {
		JadxDecompiler jadx = mainWindow.getWrapper().getDecompiler();
		HashMap<String, ArrayList<MethodInfo>> methodInfos = new HashMap<>();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		for (JavaClass klass : jadx.getClasses()) {
			for (JavaMethod method : klass.getMethods()) {
				if (method.getAccessFlags().isNative()) {
					String key = method.getFullName();
					ArrayList<MethodInfo> overloadMethods = methodInfos.getOrDefault(key, null);
					if (overloadMethods == null) {
						overloadMethods = new ArrayList<>();
						overloadMethods.add(new MethodInfo(method));
						methodInfos.put(key, overloadMethods);
					} else {
						overloadMethods.add(new MethodInfo(method));
						// methodInfos.put(key, overloadMethods);
					}
				}
			}
		}
		// get formatted output
		HashMap<String, MethodInfo> result = new HashMap<>();
		Iterator it = methodInfos.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			String name = (String) e.getKey();
			ArrayList<MethodInfo> overloadMethods = (ArrayList<MethodInfo>) e.getValue();
			boolean isOverload = overloadMethods.size() > 1 ? true : false;
			for (MethodInfo m: overloadMethods) {
				String nativeName = m.getNativeName(name, isOverload);
				assert !result.containsKey(nativeName);
				result.put(nativeName, m);
			}
		}

		try {
			FileWriter outfile = new FileWriter(mainWindow.getWrapper().getOpenPaths().get(0).toString() + "_jni_sign.json");
			outfile.append(gson.toJson(result));
			outfile.flush();
			outfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(mainWindow,"lazy jni says: export jni signature success!");
	}
}
