package com.hazelcast.client.impl.protocol.template;

import com.hazelcast.annotation.GenerateCodec;
import com.hazelcast.annotation.Nullable;
import com.hazelcast.annotation.Response;

import java.util.Map;
import java.util.List;

import com.hazelcast.nio.Address;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.map.impl.SimpleEntryView;
import com.hazelcast.mapreduce.JobPartitionState;
import com.hazelcast.client.impl.client.DistributedObjectInfo;
import com.hazelcast.client.impl.protocol.ResponseMessageConst;


@GenerateCodec(id = 0, name = "response", ns = "")
public interface ResponseTemplate {

    @Response(ResponseMessageConst.VOID)
    void Void();

    /**
     * @param response True if operation is successful, false otherwise.
     */
    @Response(ResponseMessageConst.BOOLEAN)
    void Boolean(boolean response);

    /**
     * @param response The operation result as an integer.
     */
    @Response(ResponseMessageConst.INTEGER)
    void Integer(int response);

    /**
     * @param response The operation result as a long.
     */
    @Response(ResponseMessageConst.LONG)
    void Long(long response);

    /**
     * @param response The operation result as a string.
     */
    @Response(ResponseMessageConst.STRING)
    void String(String response);

    /**
     * @param response The operation result as a serialized byte-array.
     */
    @Response(ResponseMessageConst.DATA)
    void Data(@Nullable Data response);

    /**
     * @param response The operation result as an array of serialized byte-array.
     */
    @Response(ResponseMessageConst.LIST_DATA)
    void ListData(List<Data> response);

    /**
     * @param response The operation result as an array of serialized key-value byte-arrays.
     */
    @Response(ResponseMessageConst.LIST_ENTRY)
    void ListEntry(List<Map.Entry<Data, Data>> response);

    /**
     * @param status               Authentication result as 0:AUTHENTICATED, 1:CREDENTIALS_FAILED, 2:SERIALIZATION_VERSION_MISMATCH
     * @param address              The address of the member server. This value will be null if status is not 0
     * @param uuid                 Unique string identifying the connected client uniquely. This string is generated by the owner member server
     *                             on initial connection. When the client connects to a non-owner member it sets this field on the request.
     *                             This value will be null if status is not 0
     * @param ownerUuid            Unique string identifying the server member uniquely.This value will be null if status is not 0
     * @param serializationVersion server side supported serialization version.
     *                             This value should be used for setting serialization version if status is 2
     * @return Returns the address, uuid and owner uuid.
     */
    @Response(ResponseMessageConst.AUTHENTICATION)
    Object Authentication(byte status, @Nullable Address address, @Nullable String uuid, @Nullable String ownerUuid,
                          byte serializationVersion);

    /**
     * @param partitions mappings from member address to list of partition id 's that member owns
     */
    @Response(ResponseMessageConst.PARTITIONS)
    void Partitions(Map<Address, List<Integer>> partitions);

    /**
     * @param response An list of DistributedObjectInfo (service name and object name).
     */
    @Response(ResponseMessageConst.LIST_DISTRIBUTED_OBJECT)
    void ListDistributedObject(List<DistributedObjectInfo> response);

    /**
     * @param response Response as an EntryView Data type.
     */
    @Response(ResponseMessageConst.ENTRY_VIEW)
    void EntryView(@Nullable SimpleEntryView<Data, Data> response);

    /**
     * @param jobPartitionStates The state of the job. See Job Partition State Data Type description for details.
     * @param processRecords     Number of processed records.
     * @return The information about the job if exists
     */
    @Response(ResponseMessageConst.JOB_PROCESS_INFO)
    Object JobProcessInfo(List<JobPartitionState> jobPartitionStates, int processRecords);

    /**
     * @param tableIndex the last tableIndex processed
     * @param keys       list of keys
     */
    @Response(ResponseMessageConst.CACHE_KEY_ITERATOR_RESULT)
    void CacheKeyIteratorResult(int tableIndex, List<Data> keys);

    /**
     * @param errorCode      error code of this exception
     * @param className      java class name of exception
     * @param message        details of exception
     * @param stacktrace     array of stack trace
     * @param causeErrorCode error code of cause of this exception, if there is no cause -1
     * @param causeClassName java class name of the cause of this exception
     */
    @Response(ResponseMessageConst.EXCEPTION)
    void Exception(int errorCode, String className, @Nullable String message, StackTraceElement[] stacktrace
            , int causeErrorCode, @Nullable String causeClassName);

    /**
     * @param readCount Number of items in the response.
     * @param items     The array of serialized items.
     */
    @Response(ResponseMessageConst.READ_RESULT_SET)
    void ReadResultSet(int readCount, List<Data> items);

}
