
package com.jfixby.r3.s3.assets.manager.setup;

import java.io.IOException;

import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Mapping;
import com.jfixby.scarabei.api.desktop.DesktopSetup;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.sys.Sys;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class RemoveHttpFolderDescriptor {

	public static void main (final String[] args) throws IOException {

// http.folder-descriptor

		DesktopSetup.deploy();
		Json.installComponent("com.jfixby.cmns.adopted.gdx.json.RedJson");

		final Mapping<String, S3BankSettings> availableSettings = S3BankSettings.loadSettings();
		{
			final String bankName = "com.red-triplane.assets.r3";
			clean(bankName, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.tinto";
			clean(bankName, availableSettings);
		}

	}

	private static void clean (final String bankName, final Mapping<String, S3BankSettings> availableSettings) throws IOException {

		L.d("cleaning bank", bankName);

		final S3BankSettings bankSettings = availableSettings.get(bankName);
		if (bankSettings == null) {
			L.d("Missing settings", bankName + "");
			availableSettings.print("list");
			Err.reportError("");
		}

		final File workspace_folder = LocalFileSystem.newFile(EnvironmentConfig.WORKSPACE_FOLDER);
		final EclipseWorkSpaceSettings workspace_settings = EclipseWorkSpaceSettings.readWorkspaceSettings(workspace_folder);
		L.d("settings", bankSettings);
		final EclipseProjectInfo projectInfo = workspace_settings.getProjectInfo(bankSettings.local_container_name);
		final File projectFolder = projectInfo.getProjectPath();

		final List<File> toDelete = projectFolder.listAllChildren()
			.filter( (file) -> file.getName().startsWith("http.folder-descriptor"));

		toDelete.print("toDelete");
		Sys.exit();
		Collections.scanCollection(toDelete, (x, y) -> {
			try {
				x.delete();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		});
	}

}
