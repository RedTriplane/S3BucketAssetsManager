
package com.jfixby.r3.s3.assets.manager;

import java.io.IOException;

import com.jfixby.cmns.adopted.gdx.json.RedJson;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.json.Json;
import com.jfixby.cmns.api.json.JsonString;
import com.jfixby.cmns.api.log.L;
import com.jfixby.red.desktop.DesktopSetup;

public class CreateSettings {

	public static void main (final String[] args) throws IOException {
		DesktopSetup.deploy();
		Json.installComponent(new RedJson());

		final S3BankSettings settings = new S3BankSettings();
		settings.bank_name = "bank-r3";
		settings.s3_bucket_name = "com.red-triplane.assets";
		settings.s3_bucket_bank_folder_name = "bank-r3";
		settings.s3_bucket_host = "s3.eu-central-1.amazonaws.com";
		{
			final TankInfo tank = new TankInfo();
			tank.name = "tank-0";
			settings.tanks.add(tank);
		}
		final File settingsFolder = LocalFileSystem.ApplicationHome().child(TAGS.SETTINGS_FOLDER_NAME);
		settingsFolder.makeFolder();

		final File settingsFile = settingsFolder.child("example-settings.json");

		final JsonString dataString = Json.serializeToString(settings);
		L.d("writing", settingsFile);
		settingsFile.writeString(dataString.toString());

	}

}
