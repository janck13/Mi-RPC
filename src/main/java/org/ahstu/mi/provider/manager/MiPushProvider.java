package org.ahstu.mi.provider.manager;

import com.alibaba.fastjson.JSON;
import org.ahstu.mi.common.StringUtil;
import org.ahstu.mi.common.MiConstants;
import org.ahstu.mi.common.MiError;
import org.ahstu.mi.common.MiLogger;
import org.ahstu.mi.common.MiUtil;
import org.ahstu.mi.module.ServiceMeta;
import org.ahstu.mi.provider.MiProviderMeta;
import org.ahstu.mi.provider.MiProviderStore;
import org.ahstu.mi.provider.factory.ProviderFactory;
import org.ahstu.mi.zk.MiZkClient;
import org.ahstu.mi.zk.api.IZkClient;

/**
 * Created by renyueliang on 17/5/22.
 */
public class MiPushProvider {

    public static void push(ServiceMeta serviceMeta){

        MiLogger.record(StringUtil.format("MiPushProvider.push start ! json:"+ JSON.toJSONString(serviceMeta)));

        IZkClient zkClient =  MiZkClient.getInstance();

        //--/mi/consumer/forservice/group/com.xxx.service/version/ip
        //--/mi/prodiver/forservice/group/com.xxx.service/version/ip

        String groupPath = MiUtil.getProviderZkPath()+ MiConstants.MI_ZK_SLASH+serviceMeta.getGroup();
        String serviceGroupPath =groupPath+ MiConstants.MI_ZK_SLASH+serviceMeta.getInterfaceName();
        String versionServiceGroupPath = serviceGroupPath+ MiConstants.MI_ZK_SLASH+serviceMeta.getVersion();
        String versionServiceGroupPathAndIpPort=versionServiceGroupPath+ MiConstants.MI_ZK_SLASH
                +serviceMeta.getIp()
                + MiConstants.LOWER_HORIZONTAL_LINE
                +serviceMeta.getPort();

        try {

            if (!zkClient.has(groupPath)) {
                zkClient.addNode(groupPath, false);
            }
            if (!zkClient.has(serviceGroupPath)) {
                zkClient.addNode(serviceGroupPath, false);
            }
            if (!zkClient.has(versionServiceGroupPath)) {
                zkClient.addNode(versionServiceGroupPath, false);
            }
            if(!zkClient.has(versionServiceGroupPathAndIpPort)){
                zkClient.addNode(versionServiceGroupPathAndIpPort,true);
            }

            zkClient.setDataForStr(versionServiceGroupPathAndIpPort,MiUtil.serviceMetaToJson(serviceMeta),-1);

            MiLogger.record(StringUtil.format("MiSpringProviderBean.push success ! path:%s json:%s",versionServiceGroupPathAndIpPort,JSON.toJSONString(serviceMeta)));


        }catch (Throwable e){

            MiLogger.record(StringUtil.format("MiSpringProviderBean.push error ! versionServiceGroupPath:%s %s errorCode:%s"
                    ,versionServiceGroupPath,
                    MiError.SERVICE_META_REGISTER_EXCEPTION.getErrorCode(),
                    e.getMessage()
            ),e);

        }

    }

    public static void pushAll(){

        for(MiProviderMeta providerMeta : MiProviderStore.getAll()){
            push(ProviderFactory.miProviderMetaToServiceMeta(providerMeta));
        }

    }

}
