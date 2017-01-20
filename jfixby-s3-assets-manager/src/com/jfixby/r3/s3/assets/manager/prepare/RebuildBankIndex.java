
package com.jfixby.r3.s3.assets.manager.prepare;

import java.io.IOException;

import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.rana.api.pkg.bank.BankHeaderInfo;
import com.jfixby.rana.bank.index.IndexRebuilder;
import com.jfixby.rana.bank.index.IndexRebuilderParams;
import com.jfixby.scarabei.adopted.gdx.json.RedJson;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Mapping;
import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.desktop.DesktopSetup;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class RebuildBankIndex {

	public static void main (final String[] args) throws IOException {
		DesktopSetup.deploy();
		Json.installComponent(new RedJson());

		final Mapping<String, S3BankSettings> availableSettings = S3BankSettings.loadSettings();
		{
			final String bankName = "com.red-triplane.assets.r3";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// rebuildBank(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.tinto";
			final List<String> tanksToProcess = Collections.newList("tank-0");
			rebuildBank(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.lib";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// rebuildBank(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.parallax";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// rebuildBank(bankName, tanksToProcess, availableSettings);
		}
	}

	private static void rebuildBank (final String bankName, final List<String> tanksToProcess,
		final Mapping<String, S3BankSettings> availableSettings) throws IOException {
		Debug.checkNull("bankName", bankName);
		Debug.checkEmpty("bankName", bankName);
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

		final File bankFolder = assetsFolder.child(bankSettings.local_folder_name);

		final IndexRebuilderParams rebuilderParams = new IndexRebuilderParams();
		rebuilderParams.setBankFolder(bankFolder);
		rebuilderParams.addTanksToIndex(tanksToProcess);

		IndexRebuilder.rebuild(rebuilderParams);

		final File headerFile = bankFolder.child(BankHeaderInfo.FILE_NAME);
		final BankHeaderInfo info = new BankHeaderInfo();
		info.bank_name = bankName;
		headerFile.writeString(Json.serializeToString(info).toString());
		L.d("writing header file", headerFile + " " + headerFile.exists());
	}

}
