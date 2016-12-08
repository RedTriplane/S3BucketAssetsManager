
package com.jfixby.r3.s3.assets.manager.setup;

import java.io.IOException;

import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.desktop.DesktopSetup;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.ChildrenList;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.json.Json;
import com.jfixby.cmns.api.json.JsonString;
import com.jfixby.cmns.api.log.L;
import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.r3.s3.assets.manager.TankInfo;
import com.jfixby.rana.api.pkg.bank.BankHeaderInfo;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class CreateExampleSettings {

	public static void main (final String[] args) throws IOException {
		DesktopSetup.deploy();
		Json.installComponent("com.jfixby.cmns.adopted.gdx.json.RedJson");

		final File workspace_folder = LocalFileSystem.newFile(EnvironmentConfig.WORKSPACE_FOLDER);
		final EclipseWorkSpaceSettings workspace_settings = EclipseWorkSpaceSettings.readWorkspaceSettings(workspace_folder);

		final EclipseProjectInfo projectInfo = workspace_settings.getProjectInfo(EnvironmentConfig.R3AssetProjectName);
		final File projectFolder = projectInfo.getProjectPath();
		final File assetsFolder = projectFolder.child(EnvironmentConfig.ASSETS_ROOT_FOLDER_NAME);
		final File bankFolder = assetsFolder.child("bank-r3");
		if (!(bankFolder.isFolder() && bankFolder.child(BankHeaderInfo.FILE_NAME).exists())) {
			Err.reportError("is not bank: " + bankFolder);
		}
		L.d("local copy", bankFolder);

		final S3BankSettings settings = new S3BankSettings();
		settings.bank_name = "com.red-triplane.assets.r3";
		settings.s3_bucket_name = "com.red-triplane.assets";
		settings.s3_bucket_bank_folder_name = "bank-r3";
		settings.s3_bucket_host = "s3.eu-central-1.amazonaws.com";

		settings.local_container_name = EnvironmentConfig.R3AssetProjectName;
		settings.local_folder_name = "bank-r3";

		final ChildrenList children = bankFolder.listDirectChildren(file -> {
			try {
				return file.isFolder();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		});

// children.print("?");
// Sys.exit();
		Collections.scanCollection(children, (file, index) -> {
			try {
				if (!file.isFolder()) {
					return;
				}

				final TankInfo tank = new TankInfo();
				tank.name = file.getName();
				settings.tanks.add(tank);
				L.d("tank found", file);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		});

		final File settingsFolder = LocalFileSystem.ApplicationHome().child(EnvironmentConfig.SETTINGS_FOLDER_NAME);
		settingsFolder.makeFolder();

		final File settingsFile = settingsFolder.child("bank-r3-settings.json");

		final JsonString dataString = Json.serializeToString(settings);
		L.d("writing", settingsFile);
		settingsFile.writeString(dataString.toString());

	}

}
