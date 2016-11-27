
package com.jfixby.r3.s3.assets.manager;

import java.io.IOException;

import com.jfixby.amazon.aws.s3.AWSS3FileSystem;
import com.jfixby.amazon.aws.s3.AWSS3FileSystemConfig;
import com.jfixby.cmns.adopted.gdx.json.RedJson;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.json.Json;
import com.jfixby.cmns.api.json.JsonString;
import com.jfixby.cmns.api.log.L;
import com.jfixby.red.desktop.DesktopSetup;

public class DeployBankR3 {

	public static void main (final String[] args) throws IOException {
		DesktopSetup.deploy();
		Json.installComponent(new RedJson());

		final String settingsFileName = "bank-r3.json";

		final File settingsFolder = LocalFileSystem.ApplicationHome().child(TAGS.SETTINGS_FOLDER_NAME);

		final File settingsFile = settingsFolder.child(settingsFileName);
		if (!settingsFile.exists()) {
			Err.reportError("file not found " + settingsFolder);
		}

		final String rawString = settingsFile.readToString();
		final JsonString jsonString = Json.newJsonString(rawString);
		final S3BankSettings settings = Json.deserializeFromString(S3BankSettings.class, jsonString);

		final AWSS3FileSystemConfig aws_specs = new AWSS3FileSystemConfig();
		aws_specs.setBucketName(settings.s3_bucket_name);//
		final AWSS3FileSystem S3 = new AWSS3FileSystem(aws_specs);
		for (int i = 0; i < settings.tanks.size(); i++) {
			final TankInfo tank = settings.tanks.get(i);
			final File remoteTankFolder = S3.ROOT().child(settings.s3_bucket_bank_folder_name).child(tank.name);
			L.d("make folder", remoteTankFolder);
			remoteTankFolder.makeFolder();
		}

	}

}
