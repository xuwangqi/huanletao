package cn.huanletao.redis.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

@Configuration
@ConfigurationProperties(prefix = "huanletao.cluster")
public class JedisClusterConfig {
	private List<String> nodes;
	private Integer maxTotal;
	private Integer maxIdle;
	private Integer minIdle;
	/*
	 * jedisCluster初始化方法
	 */
	@Bean
	public JedisCluster initCluster() {
		//TOOD 收集节点信息，配置对象，构造jedisCluster过程
		Set<HostAndPort> set=new HashSet<HostAndPort>();
		//nodes={10.9104.184:8000","10.9.104.184:8001"}
		for(String node:nodes) {
			//ip port node="10..104.184:8000"
			String hostIp=node.split(":")[0];
			int port=Integer.parseInt(node.split(":")[1]);
			set.add(new HostAndPort(hostIp, port));

		}
		//配置对象config
		GenericObjectPoolConfig config=new GenericObjectPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMinIdle(minIdle);
		config.setMaxIdle(maxIdle);
		//构建jediscluster
		return new JedisCluster(set,config);
	}
	
	
	public List<String> getNodes() {
		return nodes;
	}
	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}
	public Integer getMaxTotal() {
		return maxTotal;
	}
	public void setMaxTotal(Integer maxTotal) {
		this.maxTotal = maxTotal;
	}
	public Integer getMaxIdle() {
		return maxIdle;
	}
	public void setMaxIdle(Integer maxIdle) {
		this.maxIdle = maxIdle;
	}
	public Integer getMinIdle() {
		return minIdle;
	}
	public void setMinIdle(Integer minIdle) {
		this.minIdle = minIdle;
	}

}
