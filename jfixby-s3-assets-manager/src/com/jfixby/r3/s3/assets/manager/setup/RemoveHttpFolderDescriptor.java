
package com.jfixby.r3.s3.assets.manager.setup;

import java.io.IOException;

import com.jfixby.cmns.adopted.gdx.json.RedJson;
import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.collections.Mapping;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.json.Json;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.sys.Sys;
import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.red.desktop.DesktopSetup;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class RemoveHttpFolderDescriptor {

	public static void main (final String[] args) throws IOException {

// http.folder-descriptor

		DesktopSetup.deploy();
		Json.installComponent(new RedJson());

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
