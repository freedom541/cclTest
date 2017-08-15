package com.ccl.jersey.codegen;

import com.google.common.base.Function;
import com.mysema.codegen.AbstractCodeWriter;
import com.mysema.codegen.CodeWriter;
import com.mysema.codegen.CodegenException;
import com.mysema.codegen.StringUtils;
import com.mysema.codegen.model.Parameter;
import com.mysema.codegen.model.Type;
import com.mysema.codegen.model.TypeCategory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.mysema.codegen.Symbols.*;

/**
 * MyJavaWriter is the default implementation of the CodeWriter interface
 * 
 * @author ccl
 * 
 */
public final class MyJavaWriter extends AbstractCodeWriter<MyJavaWriter> {

	private static final String EXTENDS = " extends ";

	private static final String IMPLEMENTS = " implements ";

	private static final String IMPORT = "import ";

	private static final String IMPORT_STATIC = "import static ";

	private static final String PACKAGE = "package ";

	private static final String PRIVATE = "private ";

	private static final String PRIVATE_FINAL = "private final ";

	private static final String PRIVATE_STATIC_FINAL = "private static final ";

	private static final String PROTECTED = "protected ";

	private static final String PROTECTED_FINAL = "protected final ";

	private static final String PUBLIC = "public ";

	private static final String PUBLIC_CLASS = "public class ";
	
	private static final String PUBLIC_ENUM = "public enum ";

	private static final String PUBLIC_FINAL = "public final ";

	private static final String PUBLIC_INTERFACE = "public interface ";

	private static final String PUBLIC_STATIC = "public static ";

	private static final String PUBLIC_STATIC_FINAL = "public static final ";

	private final Set<String> classes = new HashSet<String>();

	private final Set<String> packages = new HashSet<String>();

	private final Stack<Type> types = new Stack<Type>();

	public MyJavaWriter(Appendable appendable) {
		super(appendable, 4);
		this.packages.add("java.lang");
	}

	@Override
	public MyJavaWriter annotation(Annotation annotation) throws IOException {
		beginLine().append("@").appendType(annotation.annotationType());
		Method[] methods = annotation.annotationType().getDeclaredMethods();
		if (methods.length == 1 && methods[0].getName().equals("value")) {
			try {
				Object value = methods[0].invoke(annotation);
				append("(");
				annotationConstant(value);
				append(")");
			} catch (IllegalArgumentException e) {
				throw new CodegenException(e);
			} catch (IllegalAccessException e) {
				throw new CodegenException(e);
			} catch (InvocationTargetException e) {
				throw new CodegenException(e);
			}
		} else {
			boolean first = true;
			for (Method method : methods) {
				try {
					Object value = method.invoke(annotation);
					if (value == null || value.equals(method.getDefaultValue())) {
						continue;
					} else if (value.getClass().isArray()
							&& Arrays.equals((Object[]) value,
									(Object[]) method.getDefaultValue())) {
						continue;
					} else if (!first) {
						append(COMMA);
					} else {
						append("(");
					}
					append(method.getName() + "=");
					annotationConstant(value);
				} catch (IllegalArgumentException e) {
					throw new CodegenException(e);
				} catch (IllegalAccessException e) {
					throw new CodegenException(e);
				} catch (InvocationTargetException e) {
					throw new CodegenException(e);
				}
				first = false;
			}
			if (!first) {
				append(")");
			}
		}
		return nl();
	}

	@Override
	public MyJavaWriter annotation(Class<? extends Annotation> annotation)
			throws IOException {
		return beginLine().append("@").appendType(annotation).nl();
	}

	@SuppressWarnings("rawtypes")
	private void annotationConstant(Object value) throws IOException {
		if (value.getClass().isArray()) {
			append("{");
			boolean first = true;
			for (Object o : (Object[]) value) {
				if (!first) {
					append(", ");
				}
				annotationConstant(o);
				first = false;
			}
			append("}");
		} else if (value instanceof Class) {
			appendType((Class) value).append(".class");
		} else if (value instanceof Number || value instanceof Boolean) {
			append(value.toString());
		} else if (value instanceof Enum) {
			Enum enumValue = (Enum) value;
			if (classes.contains(enumValue.getClass().getName())
					|| packages.contains(enumValue.getClass().getPackage()
							.getName())) {
				append(enumValue.name());
			} else {
				append(enumValue.getDeclaringClass().getName() + DOT
						+ enumValue.name());
			}
		} else if (value instanceof String) {
			String escaped = StringUtils.escapeJava(value.toString());
			append(QUOTE + escaped.replace("\\/", "/") + QUOTE);
		} else {
			throw new IllegalArgumentException(
					"Unsupported annotation value : " + value);
		}
	}

	private MyJavaWriter appendType(Class<?> type) throws IOException {
		if (classes.contains(type.getName())
				|| packages.contains(type.getPackage().getName())) {
			append(type.getSimpleName());
		} else {
			append(type.getName());
		}
		return this;
	}

	@Override
	public MyJavaWriter beginClass(Type type) throws IOException {
		return beginClass(type, null);
	}

	@Override
	public MyJavaWriter beginClass(Type type, Type superClass,
			Type... interfaces) throws IOException {
		packages.add(type.getPackageName());
		if (TypeCategory.ENUM.equals(type.getCategory())) {
			beginLine(PUBLIC_ENUM + type.getGenericName(false, packages, classes));
		} else {
			beginLine(PUBLIC_CLASS + type.getGenericName(false, packages, classes));
		}
		if (superClass != null) {
			append(EXTENDS
					+ superClass.getGenericName(false, packages, classes));
		}
		if (interfaces.length > 0) {
			append(IMPLEMENTS);
			for (int i = 0; i < interfaces.length; i++) {
				if (i > 0) {
					append(COMMA);
				}
				append(interfaces[i].getGenericName(false, packages, classes));
			}
		}
		append(" {").nl().nl();
		goIn();
		types.push(type);
		return this;
	}

	@Override
	public <T> MyJavaWriter beginConstructor(Collection<T> parameters,
			Function<T, Parameter> transformer) throws IOException {
		types.push(types.peek());
		beginLine(PUBLIC + types.peek().getSimpleName())
				.params(parameters, transformer).append(" {").nl();
		return goIn();
	}

	@Override
	public MyJavaWriter beginConstructor(Parameter... parameters)
			throws IOException {
		types.push(types.peek());
		beginLine(PUBLIC + types.peek().getSimpleName()).params(parameters)
				.append(" {").nl();
		return goIn();
	}

	@Override
	public MyJavaWriter beginInterface(Type type, Type... interfaces)
			throws IOException {
		packages.add(type.getPackageName());
		beginLine(PUBLIC_INTERFACE
				+ type.getGenericName(false, packages, classes));
		if (interfaces.length > 0) {
			append(EXTENDS);
			for (int i = 0; i < interfaces.length; i++) {
				if (i > 0) {
					append(COMMA);
				}
				append(interfaces[i].getGenericName(false, packages, classes));
			}
		}
		append(" {").nl().nl();
		goIn();
		types.push(type);
		return this;
	}

	private MyJavaWriter beginMethod(String modifiers, Type returnType,
			String methodName, Parameter... args) throws IOException {
		types.push(types.peek());
		beginLine(
				modifiers + returnType.getGenericName(true, packages, classes)
						+ SPACE + methodName).params(args).append(" {").nl();
		return goIn();
	}

	@Override
	public <T> MyJavaWriter beginPublicMethod(Type returnType,
			String methodName, Collection<T> parameters,
			Function<T, Parameter> transformer) throws IOException {
		return beginMethod(PUBLIC, returnType, methodName,
				transform(parameters, transformer));
	}

	@Override
	public MyJavaWriter beginPublicMethod(Type returnType, String methodName,
			Parameter... args) throws IOException {
		return beginMethod(PUBLIC, returnType, methodName, args);
	}

	@Override
	public <T> MyJavaWriter beginStaticMethod(Type returnType,
			String methodName, Collection<T> parameters,
			Function<T, Parameter> transformer) throws IOException {
		return beginMethod(PUBLIC_STATIC, returnType, methodName,
				transform(parameters, transformer));
	}

	@Override
	public MyJavaWriter beginStaticMethod(Type returnType, String methodName,
			Parameter... args) throws IOException {
		return beginMethod(PUBLIC_STATIC, returnType, methodName, args);
	}

	@Override
	public MyJavaWriter end() throws IOException {
		types.pop();
		goOut();
		return line("}").nl();
	}

	@Override
	public MyJavaWriter field(Type type, String name) throws IOException {
		return line(
				type.getGenericName(true, packages, classes) + SPACE + name
						+ SEMICOLON).nl();
	}

	private MyJavaWriter field(String modifier, Type type, String name)
			throws IOException {
		return line(
				modifier + type.getGenericName(true, packages, classes) + SPACE
						+ name + SEMICOLON).nl();
	}

	private MyJavaWriter field(String modifier, Type type, String name,
			String value) throws IOException {
		return line(
				modifier + type.getGenericName(true, packages, classes) + SPACE
						+ name + ASSIGN + value + SEMICOLON).nl();
	}

	@Override
	public String getClassConstant(String className) {
		return className + ".class";
	}

	@Override
	public String getGenericName(boolean asArgType, Type type) {
		return type.getGenericName(asArgType, packages, classes);
	}

	@Override
	public String getRawName(Type type) {
		return type.getRawName(packages, classes);
	}

	@Override
	public MyJavaWriter imports(Class<?>... imports) throws IOException {
		for (Class<?> cl : imports) {
			classes.add(cl.getName());
			line(IMPORT + cl.getName() + SEMICOLON);
		}
		nl();
		return this;
	}

	@Override
	public MyJavaWriter imports(Package... imports) throws IOException {
		for (Package p : imports) {
			packages.add(p.getName());
			line(IMPORT + p.getName() + ".*;");
		}
		nl();
		return this;
	}

	@Override
	public MyJavaWriter importClasses(String... imports) throws IOException {
		for (String cl : imports) {
			classes.add(cl);
			line(IMPORT + cl + SEMICOLON);
		}
		nl();
		return this;
	}

	@Override
	public MyJavaWriter importPackages(String... imports) throws IOException {
		for (String p : imports) {
			packages.add(p);
			line(IMPORT + p + ".*;");
		}
		nl();
		return this;
	}

	@Override
	public MyJavaWriter javadoc(String... lines) throws IOException {
		line("/**");
		for (String line : lines) {
			line(" * " + line);
		}
		return line(" */");
	}

	@Override
	public MyJavaWriter packageDecl(String packageName) throws IOException {
		packages.add(packageName);
		return line(PACKAGE + packageName + SEMICOLON).nl();
	}

	private <T> MyJavaWriter params(Collection<T> parameters,
			Function<T, Parameter> transformer) throws IOException {
		append("(");
		boolean first = true;
		for (T param : parameters) {
			if (!first) {
				append(COMMA);
			}
			param(transformer.apply(param));
			first = false;
		}
		append(")");
		return this;
	}

	private MyJavaWriter params(Parameter... params) throws IOException {
		append("(");
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				append(COMMA);
			}
			param(params[i]);
		}
		append(")");
		return this;
	}

	private MyJavaWriter param(Parameter parameter) throws IOException {
		append(parameter.getType().getGenericName(true, packages, classes));
		append(" ");
		append(parameter.getName());
		return this;
	}

	@Override
	public MyJavaWriter privateField(Type type, String name) throws IOException {
		return field(PRIVATE, type, name);
	}

	@Override
	public MyJavaWriter privateFinal(Type type, String name) throws IOException {
		return field(PRIVATE_FINAL, type, name);
	}

	@Override
	public MyJavaWriter privateFinal(Type type, String name, String value)
			throws IOException {
		return field(PRIVATE_FINAL, type, name, value);
	}

	@Override
	public MyJavaWriter privateStaticFinal(Type type, String name, String value)
			throws IOException {
		return field(PRIVATE_STATIC_FINAL, type, name, value);
	}

	@Override
	public MyJavaWriter protectedField(Type type, String name)
			throws IOException {
		return field(PROTECTED, type, name);
	}

	@Override
	public MyJavaWriter protectedFinal(Type type, String name)
			throws IOException {
		return field(PROTECTED_FINAL, type, name);
	}

	@Override
	public MyJavaWriter protectedFinal(Type type, String name, String value)
			throws IOException {
		return field(PROTECTED_FINAL, type, name, value);
	}

	@Override
	public MyJavaWriter publicField(Type type, String name) throws IOException {
		return field(PUBLIC, type, name);
	}

	@Override
	public MyJavaWriter publicField(Type type, String name, String value)
			throws IOException {
		return field(PUBLIC, type, name, value);
	}

	@Override
	public MyJavaWriter publicFinal(Type type, String name) throws IOException {
		return field(PUBLIC_FINAL, type, name);
	}

	@Override
	public MyJavaWriter publicFinal(Type type, String name, String value)
			throws IOException {
		return field(PUBLIC_FINAL, type, name, value);
	}

	@Override
	public MyJavaWriter publicStaticFinal(Type type, String name, String value)
			throws IOException {
		return field(PUBLIC_STATIC_FINAL, type, name, value);
	}

	@Override
	public MyJavaWriter staticimports(Class<?>... imports) throws IOException {
		for (Class<?> cl : imports) {
			line(IMPORT_STATIC + cl.getName() + ".*;");
		}
		return this;
	}

	@Override
	public MyJavaWriter suppressWarnings(String type) throws IOException {
		return line("@SuppressWarnings(\"" + type + "\")");
	}

    @Override
    public CodeWriter suppressWarnings(String... strings) throws IOException {
        return null;
    }

    private <T> Parameter[] transform(Collection<T> parameters,
			Function<T, Parameter> transformer) {
		Parameter[] rv = new Parameter[parameters.size()];
		int i = 0;
		for (T value : parameters) {
			rv[i++] = transformer.apply(value);
		}
		return rv;
	}

}
