
package com.jfixby.r3.s3.assets.manager;

import java.io.IOException;
import java.util.ArrayList;

import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.Map;
import com.jfixby.scarabei.api.collections.Mapping;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FilesList;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpURL;

public class S3BankSettings {

	public static final String FILE_NAME = "s3-bank-settings.json";
	public String bank_name = "";

	public String s3_bucket_host;
	public String s3_bucket_name;
	public String s3_bucket_bank_folder_name;

	public ArrayList<TankInfo> tanks = new ArrayList<>();

	public String local_container_name;
	public String local_bank_folder_name;

	@Override
	public String toString () {
		return "S3BankSettings [bank_name=" + this.bank_name + ", s3_bucket_host=" + this.s3_bucket_host + ", s3_bucket_name="
			+ this.s3_bucket_name + ", s3_bucket_bank_folder_name=" + this.s3_bucket_bank_folder_name + ", tanks=" + this.tanks
			+ ", local_container_name=" + this.local_container_name + "]";
	}

	public static Mapping<String, S3BankSettings> loadSettings () throws IOException {
		final File settingsFolder = LocalFileSystem.ApplicationHome().child(EnvironmentConfig.SETTINGS_FOLDER_NAME);
		final FilesList list = settingsFolder.listDirectChildren(file -> file.extensionIs("json"));
		final Map<String, S3BankSettings> settingsList = Collections.newMap();
		Collections.scanCollection(list, (file, i) -> {
			String raw_json;
			try {
				raw_json = file.readToString();
				final S3BankSettings settings = Json.deserializeFromString(S3BankSettings.class, raw_json);
				settingsList.put(settings.bank_name, settings);
			} catch (final IOException e) {

				e.printStackTrace();
			}
		});
		return settingsList;
	}

	public HttpURL toPublicURLString () {
		final String urlString = "https://" + this.s3_bucket_host + "/" + this.s3_bucket_name + "/"
			+ this.s3_bucket_bank_folder_name;
		final HttpURL url = Http.newURL(urlString);

		return url;
	}

}
