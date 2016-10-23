package com.onmobile.apps.ringbacktones.v2.converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.livewiremobile.store.storefront.dto.RuntimeTypeAdapterFactory;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.livewiremobile.store.storefront.dto.rbt.CallerDefault;
import com.livewiremobile.store.storefront.dto.rbt.CallerGroup;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;
import com.livewiremobile.store.storefront.dto.rbt.DateRange;
import com.livewiremobile.store.storefront.dto.rbt.RBTPlaylist;
import com.livewiremobile.store.storefront.dto.rbt.RBTProfileTone;
import com.livewiremobile.store.storefront.dto.rbt.RBTStation;
import com.livewiremobile.store.storefront.dto.rbt.RBTUGC;
import com.livewiremobile.store.storefront.dto.rbt.Shuffle;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.livewiremobile.store.storefront.dto.rbt.SystemTone;
import com.livewiremobile.store.storefront.dto.rbt.TimeRange;
import com.livewiremobile.store.storefront.dto.rbt.serializers.DateRangeTypeAdapter;
import com.livewiremobile.store.storefront.dto.rbt.serializers.TimeRangeTypeAdapter;
import com.onmobile.apps.ringbacktones.v2.dto.CallLogDTO;

public class GsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
	private static Logger logger = Logger.getLogger(GsonHttpMessageConverter.class);
    private static GsonBuilder read_gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    
    private static GsonBuilder write_gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
    
    private static GsonBuilder read_unconditioned_gsonBuilder = new GsonBuilder();
    static {
    	read_gsonBuilder.registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory(true));
        RuntimeTypeAdapterFactory<Asset> assetAdapter = RuntimeTypeAdapterFactory.of(Asset.class , "type");
        assetAdapter.registerSubtype(Song.class , AssetType.SONG.toString());
        assetAdapter.registerSubtype(Shuffle.class , AssetType.SHUFFLELIST.toString());
        assetAdapter.registerSubtype(SystemTone.class ,  AssetType.SYSTEMTONE.toString());
        assetAdapter.registerSubtype(RBTPlaylist.class ,  AssetType.RBTPLAYLIST.toString());
        assetAdapter.registerSubtype(RBTStation.class ,  AssetType.RBTSTATION.toString());
        assetAdapter.registerSubtype(RBTUGC.class ,  AssetType.RBTUGC.toString());
        assetAdapter.registerSubtype(RBTProfileTone.class ,  AssetType.RBTPROFILETONE.toString());
        
        RuntimeTypeAdapterFactory<CallingParty> callingPartyassetAdapter = RuntimeTypeAdapterFactory.of(CallingParty.class, "type");
        callingPartyassetAdapter.registerSubtype(Caller.class , CallingPartyType.CALLER.toString());
        callingPartyassetAdapter.registerSubtype(CallerDefault.class , CallingPartyType.DEFAULT.toString());
        callingPartyassetAdapter.registerSubtype(CallerGroup.class , CallingPartyType.GROUP.toString());
        
       

        read_gsonBuilder.registerTypeAdapterFactory(assetAdapter);
        read_gsonBuilder.registerTypeAdapterFactory(callingPartyassetAdapter);
        read_gsonBuilder.registerTypeAdapter(DateRange.class, new DateRangeTypeAdapter());
        read_gsonBuilder.registerTypeAdapter(TimeRange.class, new TimeRangeTypeAdapter());
        read_gsonBuilder.serializeNulls();
        
        write_gsonBuilder.registerTypeAdapterFactory(assetAdapter);
        write_gsonBuilder.registerTypeAdapterFactory(callingPartyassetAdapter);
        write_gsonBuilder.registerTypeAdapter(DateRange.class, new DateRangeTypeAdapter());
        write_gsonBuilder.registerTypeAdapter(TimeRange.class, new TimeRangeTypeAdapter());
        write_gsonBuilder.serializeNulls();
        
        
    }
    

    public static GsonBuilder getRead_gsonBuilder() {
		return read_gsonBuilder;
	}

	public static GsonBuilder getWrite_gsonBuilder() {
		return write_gsonBuilder;
	}

	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public GsonHttpMessageConverter(){
        super(new MediaType("application", "json", DEFAULT_CHARSET));
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz,
                                  HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {

        try{
        	GsonBuilder gsonBuilder = read_gsonBuilder;
        	Date requestedTimeStamp = new Date();
        	if(clazz.isInstance(new CallLogDTO[1])) {
        		gsonBuilder = read_unconditioned_gsonBuilder;
        	}
        	Object obj = gsonBuilder.create().fromJson(convertStreamToString(inputMessage.getBody()), clazz);
        	Date responseTimeStamp = new Date();
			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());
			logger.info("Parsing Time: "+ differenceTime);
            return obj;
        }catch(JsonSyntaxException e){
            throw new HttpMessageNotReadableException("Could not read JSON: " + e.getMessage(), e);
        }

    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    protected void writeInternal(Object t, 
                                 HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {

        //TODO: adapt this to be able to receive a list of json objects too
      
        String json = write_gsonBuilder.setDateFormat("yyyy-MM-dd HH:mm:ss Z").create().toJson(t);

        outputMessage.getBody().write(json.getBytes());
    }

    //TODO: move this to a more appropriated utils class
    public String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the Reader.read(char[]
         * buffer) method. We iterate until the Reader return -1 which means
         * there's no more data to read. We use the StringWriter class to
         * produce the string.
         */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    
   /* public void initCreateGson() {
        logger.debug(" initCreateGson starts ");
        GsonBuilder gsonBuilder = getRbtHelper().getGsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapterFactory(new LowercaseEnumTypeAdapterFactory(true));
        RuntimeTypeAdapterFactory<Asset> assetAdapter = RuntimeTypeAdapterFactory.of(Asset.class , "type");
        assetAdapter.registerSubtype(Song.class , AssetType.SONG.toString());
        assetAdapter.registerSubtype(Shuffle.class , AssetType.SHUFFLELIST.toString());
        assetAdapter.registerSubtype(SystemTone.class ,  AssetType.SYSTEMTONE.toString());
        assetAdapter.registerSubtype(RBTPlaylist.class ,  AssetType.RBTPLAYLIST.toString());
        assetAdapter.registerSubtype(RBTStation.class ,  AssetType.RBTSTATION.toString());

        RuntimeTypeAdapterFactory<CallingParty> callingPartyassetAdapter = RuntimeTypeAdapterFactory.of(CallingParty.class, "type");
        callingPartyassetAdapter.registerSubtype(Caller.class , CallingPartyType.CALLER.toString());

        gsonBuilder.registerTypeAdapterFactory(assetAdapter);
        gsonBuilder.registerTypeAdapterFactory(callingPartyassetAdapter);
        gsonBuilder.serializeNulls();
        getRbtHelper().setGson(gsonBuilder.create());
 }*/

}