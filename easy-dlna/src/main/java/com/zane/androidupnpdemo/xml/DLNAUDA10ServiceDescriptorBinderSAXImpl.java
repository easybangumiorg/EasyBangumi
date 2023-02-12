package com.zane.androidupnpdemo.xml;

import org.fourthline.cling.binding.staging.MutableService;
import org.fourthline.cling.binding.xml.DescriptorBindingException;
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.Service;
import org.seamless.xml.SAXParser;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * Description：DLNAUDA10ServiceDescriptorBinderSAXImpl
 * <BR/>
 * Creator：yankebin
 * <BR/>
 * CreatedAt：2019-07-10
 */
public class DLNAUDA10ServiceDescriptorBinderSAXImpl extends UDA10ServiceDescriptorBinderSAXImpl {

    private static final String TAG = DLNAUDA10ServiceDescriptorBinderSAXImpl.class.getSimpleName();

    @Override
    public <S extends Service> S describe(S undescribedService, String descriptorXml) throws DescriptorBindingException, ValidationException {

        if (descriptorXml == null || descriptorXml.length() == 0) {
            throw new DescriptorBindingException("Null or empty descriptor");
        }

        try {

            // DLNAManager.logD(TAG, "Reading service from XML descriptor, content : \n" + descriptorXml);

            SAXParser parser = new DLNASAXParser();

            MutableService descriptor = new MutableService();

            hydrateBasic(descriptor, undescribedService);

            new RootHandler(descriptor, parser);

            parser.parse(
                    new InputSource(
                            // TODO: UPNP VIOLATION: Virgin Media Superhub sends trailing spaces/newlines after last XML element, need to trim()
                            new StringReader(descriptorXml.trim())
                    )
            );

            // Build the immutable descriptor graph
            return (S) descriptor.build(undescribedService.getDevice());

        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DescriptorBindingException("Could not parse service descriptor: " + ex.toString(), ex);
        }
    }
}