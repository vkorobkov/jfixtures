package com.github.vkorobkov.jfixtures.instructions;

import com.github.vkorobkov.jfixtures.domain.Value;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.Map;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Getter
public class InsertRow implements Instruction {
    @XmlAttribute
    private static final String TYPE = "InsertRow";
    @XmlAttribute
    private String table;
    @XmlAttribute
    private String rowName;
    @XmlElement
    private Map<String, Value> values;

    public InsertRow(String table, String rowName, Map<String, Value> values) {
        this.table = table;
        this.rowName = rowName;
        this.values = Collections.unmodifiableMap(values);
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visit(this);
    }
}
