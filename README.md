import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class AdditionalField {

    @JacksonXmlProperty(isAttribute = true)
    public String name;

    @JacksonXmlText
    public String value;
}


import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JacksonXmlRootElement(localName = "acknowledgement")
public class Acknowledgement {

    @JacksonXmlProperty(localName = "brch_code")
    public String brchCode;

    @JacksonXmlProperty(localName = "filename")
    public String filename;

    @JacksonXmlProperty(localName = "ref_id")
    public String refId;

    @JacksonXmlProperty(localName = "tnx_id")
    public String tnxId;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "additional_field")
    public List<AdditionalField> additionalFields;

    // Computed map from name to value
    public Map<String, String> getAdditionalFieldMap() {
        if (additionalFields == null) return new HashMap<>();
        Map<String, String> map = new HashMap<>();
        for (AdditionalField f : additionalFields) {
            map.put(f.name, f.value);
        }
        return map;
    }
}