package eu.planets_project.tools;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

@XmlRootElement( name="toolspec" )
public class ToolSpec {

	@XmlElement
	String tool;
	
	@XmlElement
	Version version;
	
	@XmlElement
	Install install;
	
	@XmlElement
	List<Var> env;
	
	@XmlElement
	List<Var> var;
	
	@XmlElement
	List<Convert> convert;
	
	@XmlElement
	List<Validate> validate;

	
	private static JAXBContext jc;
	
	static {
		try {
			jc  = JAXBContext.newInstance(ToolSpec.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	String toXMlFormatted() throws JAXBException, UnsupportedEncodingException {
		//Create marshaller
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		m.marshal(this, bos);
		return bos.toString("UTF-8");
	}
	
	public static ToolSpec fromInputStream( InputStream input ) throws FileNotFoundException, JAXBException {
		Unmarshaller u = jc.createUnmarshaller();
		return (ToolSpec) u.unmarshal(new StreamSource(input));
	}

}
