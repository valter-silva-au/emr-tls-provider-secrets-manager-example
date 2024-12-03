package com.amazonaws.awssamples.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.util.EC2MetadataUtils;
import com.amazonaws.awssamples.retry.RetryHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class EC2TagManager {
    private final RetryHandler retryHandler;
    private final AmazonEC2 ec2Client;

    public EC2TagManager(RetryHandler retryHandler) {
        this.retryHandler = retryHandler;
        this.ec2Client = AmazonEC2ClientBuilder.defaultClient();
    }

    /**
     * Retrieves EC2 instance tags
     * @return Map of tag keys to tag values
     */
    public Map<String, String> getInstanceTags() {
        String instanceId = EC2MetadataUtils.getInstanceId();
        DescribeTagsRequest request = new DescribeTagsRequest()
            .withFilters(new Filter("resource-id", Collections.singletonList(instanceId)));

        DescribeTagsResult result = retryHandler.retry(
            r -> ec2Client.describeTags(r),
            request
        );

        Map<String, String> tags = new HashMap<>();
        if (result != null) {
            for (TagDescription tag : result.getTags()) {
                tags.put(tag.getKey(), tag.getValue());
            }
        }
        return tags;
    }

    /**
     * Gets the AWS account ID for the current instance
     */
    public String getAccountId() {
        return EC2MetadataUtils.getInstanceInfo().getAccountId();
    }

    /**
     * Gets the AWS region for the current instance
     */
    public String getRegion() {
        return EC2MetadataUtils.getInstanceInfo().getRegion();
    }
}
