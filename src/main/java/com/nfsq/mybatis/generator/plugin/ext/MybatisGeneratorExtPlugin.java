/**
 * 
 */
package com.nfsq.mybatis.generator.plugin.ext;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.rules.Rules;
import org.mybatis.generator.internal.rules.RulesDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @date 2016年10月11日 下午4:43:47 
 * @author wangk
 * @Description:
 * @project claim_web
 */
public class MybatisGeneratorExtPlugin extends PluginAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(MybatisGeneratorExtPlugin.class);
	private static final String ID_ATTR = "id";
	private static final String INSERT = "insert";
	private static final String SELECTKEY = "selectKey";
	private static final String INCLUDE = "include";
	/**
	 * @see org.mybatis.generator.api.Plugin#validate(java.util.List)
	 * @author wangk
	 * 创建日期：2016年10月11日
	 * 修改说明：
	 */
	@Override
	public boolean validate(List<String> arg0) {
		return true;
	}
	
	public void initialized(IntrospectedTable table) {
		try {
			File directory = getDirectory(table.getContext().getSqlMapGeneratorConfiguration().getTargetProject(), table.getMyBatis3XmlMapperPackage());
	        File targetFile = new File(directory, table.getMyBatis3XmlMapperFileName());
	        if(targetFile.exists()){
	        	NoClientRules myRules = new NoClientRules(table.getRules());
	        	table.setRules(myRules);
	        }
		} catch (Exception e) {
			LOG.error("MybatisGeneratorExtPlugin initialize failed!!!", e);
		}
    }
	
	@Override
	public boolean sqlMapDocumentGenerated(org.mybatis.generator.api.dom.xml.Document document,
            IntrospectedTable introspectedTable) {
		try {
			File directory = getDirectory(introspectedTable.getContext().getSqlMapGeneratorConfiguration().getTargetProject(), introspectedTable.getMyBatis3XmlMapperPackage());
	        File targetFile = new File(directory, introspectedTable.getMyBatis3XmlMapperFileName());
	        if(targetFile.exists()){
		        Document existingDocument = DocumentHelper.parseText(FileUtils.readFileToString(targetFile));
				List<org.mybatis.generator.api.dom.xml.Element> elements = document.getRootElement().getElements();
		        @SuppressWarnings("unchecked")
				List<Element> existingElements = existingDocument.getRootElement().elements();
		        for (org.mybatis.generator.api.dom.xml.Element e : elements) {
		        	if(e instanceof XmlElement){
		        		XmlElement element = (XmlElement) e;
		        		String id = null;
		        		List<org.mybatis.generator.api.dom.xml.Attribute> attributes = element.getAttributes();
		        		for (org.mybatis.generator.api.dom.xml.Attribute attribute : attributes) {
							if(ID_ATTR.equals(attribute.getName())){
								id = attribute.getValue();
							}
						}
			        	for (Element existingElement : existingElements) {
			        		String existingId = existingElement.attributeValue(ID_ATTR);
							if(id.equals(existingId)){
								if(id.startsWith(INSERT)){
				        			Element generatedKey = existingElement.element(SELECTKEY);
				        			if(generatedKey != null){
				        				org.mybatis.generator.api.dom.xml.Attribute attr = new org.mybatis.generator.api.dom.xml.Attribute("useGeneratedKeys", "false");
				        				element.addAttribute(attr);
				        				XmlElement key = new XmlElement(SELECTKEY);
				        				@SuppressWarnings("unchecked")
										Iterator<Attribute> it = generatedKey.attributeIterator();
				        				while (it.hasNext()) {
											Attribute a = (Attribute) it.next();
											key.addAttribute(new org.mybatis.generator.api.dom.xml.Attribute(a.getName(),a.getValue()));
										}
				        				TextElement t = null;
				        				Element include = generatedKey.element(INCLUDE);
				        				if(include != null){
				        					t = new TextElement("select " + include.asXML() +" from dual");
				        				}else{
				        					t = new TextElement(generatedKey.getText());
				        				}
				        				key.addElement(t);
				        				element.addElement(0, key);
				        			}
				        		}
								existingElement.detach();
								break;
							}
						}
		        	}
				}
		        FileUtils.write(targetFile, xmlFormat(existingDocument));
	        }
		} catch (Exception e) {
			LOG.error("old generated elements delete failed!!!", e);
			return false;
		}
        return true;
    }
	
	/**
	 * Format XML
	 * @Date:2016年10月13日
	 * @author wangk
	 * @param doc
	 * @return
	 * @Description:
	 */
	private String xmlFormat(Document doc){
		//创建字符串缓冲区 
        StringWriter stringWriter = new StringWriter();  
        //设置文件编码  
        OutputFormat xmlFormat = new OutputFormat();  
        xmlFormat.setEncoding("UTF-8"); 
        // 设置换行 
        xmlFormat.setNewlines(true); 
        // 生成缩进 
        xmlFormat.setIndent(true); 
        // 使用4个空格进行缩进, 可以兼容文本编辑器 
        xmlFormat.setIndent("    "); 
        
        //创建写文件方法  
        XMLWriter xmlWriter = new XMLWriter(stringWriter,xmlFormat);  
        //写入文件  
        try {
			xmlWriter.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			//关闭  
	        try {
				xmlWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        return stringWriter.toString();
	}
	
	/**
	 * 目标文件路径转换
	 * @Date:2016年10月13日
	 * @author wangk
	 * @param targetProject
	 * @param targetPackage
	 * @return
	 * @throws ShellException
	 * @Description:
	 */
	private File getDirectory(String targetProject, String targetPackage)
            throws ShellException {
        // targetProject is interpreted as a directory that must exist
        //
        // targetPackage is interpreted as a sub directory, but in package
        // format (with dots instead of slashes). The sub directory will be
        // created
        // if it does not already exist

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            throw new ShellException(getString("Warning.9", //$NON-NLS-1$
                    targetProject));
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, "."); //$NON-NLS-1$
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                throw new ShellException(getString("Warning.10", //$NON-NLS-1$
                        directory.getAbsolutePath()));
            }
        }

        return directory;
    }
	
	/**
	 * No client Rules class
	 * @date 2016年10月13日 下午3:03:23 
	 * @author wangk
	 * @Description:
	 * @project mybatis-generator-plugin-ext
	 */
	private class NoClientRules extends RulesDelegate{

		public NoClientRules(Rules rules) {
			super(rules);
		}
		
		public boolean generateJavaClient() {
	        return false;
	    }
		
	}

}
