
package com.jfixby.r3.s3.assets.manager.prepare;

import java.io.IOException;

import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Mapping;
import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.desktop.ScarabeiDesktop;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FolderSupportingIndexBuilderParams;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.gson.GoogleGson;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class RebuildFileSupportingIndex {

	public static void main (final String[] args) throws IOException {

		ScarabeiDesktop.deploy();
		Json.installComponent(new GoogleGson());

		final Mapping<String, S3BankSettings> availableSettings = S3BankSettings.loadSettings();
		{
			final String bankName = "com.red-triplane.assets.r3";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// rebuildFSI(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.tinto";
			final List<String> tanksToProcess = Collections.newList("tank-0");
			rebuildFSI(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.lib";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// rebuildFSI(bankName, tanksToProcess, availableSettings);
		}
	}

	private static void rebuildFSI (final String bankName, final List<String> tanksToProcess,
		final Mapping<String, S3BankSettings> availableSettings) throws IOException {
		L.d("processing bank", bankName);

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
		final File assetsFolder = projectFolder.child(EnvironmentConfig.ASSETS_ROOT_FOLDER_NAME);
		L.d("assetsFolder", assetsFolder);

		final File bankFolder = assetsFolder.child(bankSettings.local_bank_folder_name);

		for (final String tank : tanksToProcess) {
			Debug.checkNull("tank name", tank);
			final File tankFolder = bankFolder.child(tank);
			rebuildIndex(tankFolder);
		}
	}

	public static void rebuildIndex (final File targetFolder) throws IOException {

		L.d("indexing", targetFolder);

		final FolderSupportingIndexBuilderParams params = LocalFileSystem.component().newFolderSupportingIndexBuilderParams();

		params.setTarget(targetFolder);
		params.setRebuidOnlyForRoot(!true);
		params.setDebug(true);
		params.setIgnoreHashSum(!true);
		params.setNoOutput(false);
		params.setIgnoreJsonDecoderFailure(false);

		LocalFileSystem.component().rebuildFolderSupportingIndexes(params);
	}

}
