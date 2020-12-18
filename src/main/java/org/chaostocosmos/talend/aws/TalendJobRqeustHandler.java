package org.chaostocosmos.talend.aws;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class TalendJobRqeustHandler implements RequestHandler<Map<String, String>, String> {

	LambdaLogger logger;
	
	@Override
	public String handleRequest(Map<String, String> input, Context context) {
		logger = context.getLogger();
		String clazzName = input.get("class-name");
		String[] args = input.get("args").split(" ");
		try {
			invokeMainMethod(clazzName, args);
		} catch(Exception e) {
			String msg = e.getMessage();
			String stackTrace = ExceptionUtils.getStackTrace(e);
			logger.log(msg);
			logger.log(stackTrace);
		}
		return "SUCCESS";
	}
	
	public void invokeMainMethod(String className, String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		//String location = ClassLoader.getSystemResource(className).toString();
		//logger.log(location);
		ClassLoader classLoader = this.getClass().getClassLoader();
		Class clazz = classLoader.loadClass(className);
		Method method = clazz.getMethod("main", String[].class);
		logger.log("ARGS: "+args.length+"   "+Arrays.toString(args)+"   ");
		method.invoke(null, (Object)args);
	}

}
