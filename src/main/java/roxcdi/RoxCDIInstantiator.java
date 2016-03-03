package roxcdi;

import javax.enterprise.inject.spi.CDI;

public interface RoxCDIInstantiator {

	public CDI<?> instantiateCDI() ;
	
}
