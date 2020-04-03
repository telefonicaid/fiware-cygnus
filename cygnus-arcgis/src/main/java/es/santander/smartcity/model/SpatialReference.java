/**
 * 
 */
package es.santander.smartcity.model;

/**
 * @author dmartinez
 *
 */
public enum SpatialReference {
	WGS84(4326),
	ED50(23030), //FIXME: confirmar código
	ETRS89(4258);//FIXME: confirmar código
	
	private int wkid;
	
	SpatialReference (int wkid){
		this.wkid = wkid;
	}

	public Integer getWkid() {
		return wkid;
	}
}
