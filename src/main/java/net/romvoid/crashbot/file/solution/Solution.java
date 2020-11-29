package net.romvoid.crashbot.file.solution;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Solution {

	@SerializedName("keys")
	@Expose
	public List<String> keys;
	@SerializedName("reply")
	@Expose
	public String reply;

	public Solution() {}

	/**
	 *
	 * @param keys
	 * @param reply
	 */
	public Solution(String reply, String... keys) {
		super();
		this.keys = Arrays.asList(keys);
		this.reply = reply;
	}

	public List<String> getKeys() {
		return keys;
	}

	public void setKeys(String... keys) {
		this.keys = Arrays.asList(keys);;
	}

	public Solution withKeys(String... keys) {
		this.keys = Arrays.asList(keys);;
		return this;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	public Solution withReply(String reply) {
		this.reply = reply;
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("keys", keys).append("reply", reply).toString();
	}

}