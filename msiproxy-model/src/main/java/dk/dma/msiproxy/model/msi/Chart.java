package dk.dma.msiproxy.model.msi;

import dk.dma.msiproxy.model.JsonSerializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Chart entity
 */
@XmlRootElement(name = "chart")
@XmlType(propOrder = { "chartNumber", "internationalNumber"})
public class Chart implements JsonSerializable {

    Integer id;
    String chartNumber;
    Integer internationalNumber;

    /**
     * Constructor
     */
    public Chart() {
    }

    /**
     * Returns a string representation of the chart including chart number and international number
     * @return a string representation of the chart
     */
    public String getFullChartNumber() {
        return (internationalNumber == null)
                ? chartNumber
                : String.format("%s (INT %d)", chartNumber, internationalNumber);
    }

    @XmlAttribute
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getChartNumber() {
        return chartNumber;
    }

    public void setChartNumber(String chartNumber) {
        this.chartNumber = chartNumber;
    }

    public Integer getInternationalNumber() {
        return internationalNumber;
    }

    public void setInternationalNumber(Integer internationalNumber) {
        this.internationalNumber = internationalNumber;
    }

}
