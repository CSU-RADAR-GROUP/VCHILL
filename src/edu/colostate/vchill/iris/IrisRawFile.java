/**
 * 
 */
package edu.colostate.vchill.iris;

import edu.colostate.vchill.ChillDefines;
import edu.colostate.vchill.ControlMessage;
import edu.colostate.vchill.ScaleManager;
import edu.colostate.vchill.cache.CacheMain;
import edu.colostate.vchill.chill.ChillDataHeader;
import edu.colostate.vchill.chill.ChillFieldInfo;
import edu.colostate.vchill.chill.ChillHSKHeader;
import edu.colostate.vchill.chill.ChillGenRay;
import edu.colostate.vchill.chill.ChillMomentFieldScale;
import edu.colostate.vchill.file.FileFunctions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author Joseph Hardin
 * Description: Top level class to represent IRIS raw file format.
 */
public class IrisRawFile {

	private static final ScaleManager sm = ScaleManager.getInstance();

    private static final Map<String, ChillFieldInfo> infos = new HashMap<String, ChillFieldInfo>();
	
	
	public IrisRawFile(){
		
	}
	
	public IrisRawFile(String filename){
		
	}
	
}
