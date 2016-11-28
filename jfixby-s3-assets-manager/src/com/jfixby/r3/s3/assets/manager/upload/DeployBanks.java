
package com.jfixby.r3.s3.assets.manager.upload;

import java.io.IOException;

import com.jfixby.amazon.aws.s3.AWSS3FileSystem;
import com.jfixby.amazon.aws.s3.AWSS3FileSystemConfig;
import com.jfixby.cmns.adopted.gdx.json.RedJson;
import com.jfixby.cmns.api.collections.Collections;
import com.jfixby.cmns.api.collections.List;
import com.jfixby.cmns.api.collections.Mapping;
import com.jfixby.cmns.api.debug.Debug;
import com.jfixby.cmns.api.err.Err;
import com.jfixby.cmns.api.file.ChildrenList;
import com.jfixby.cmns.api.file.File;
import com.jfixby.cmns.api.file.FileConflistResolver;
import com.jfixby.cmns.api.file.FileSystem;
import com.jfixby.cmns.api.file.LocalFileSystem;
import com.jfixby.cmns.api.json.Json;
import com.jfixby.cmns.api.log.L;
import com.jfixby.cmns.api.net.http.HttpURL;
import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.red.desktop.DesktopSetup;
import com.jfixby.red.filesystem.http.fs.RedHttpFileSystem;
import com.jfixby.red.filesystem.http.fs.RedHttpFileSystemSpecs;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class DeployBanks {

	public static void main (final String[] args) throws IOException {

		DesktopSetup.deploy();
		Json.installComponent(new RedJson());

		final Mapping<String, S3BankSettings> availableSettings = S3BankSettings.loadSettings();
		{
			final String bankName = "com.red-triplane.assets.r3";
			final List<String> tanksToProcess = Collections.newList("tank-0");
			deploy(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.tinto";
			final List<String> tanksToProcess = Collections.newList("tank-0");
			deploy(bankName, tanksToProcess, availableSettings);
		}
	}

	private static void deploy (final String bankName, final List<String> tanksToProcess,
		final Mapping<String, S3BankSettings> availableSettings) throws IOException {
		if (tanksToProcess.size() == 0) {
			return;
		}
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
		deploy(bankFolder, bankSettings);

	}

	private static void deploy (final File localBankFolder, final S3BankSettings bankSettings) throws IOException {
		Debug.checkTrue(localBankFolder + " does not exist", localBankFolder.exists());

		final String bankName = localBankFolder.getName();
		{

			final AWSS3FileSystemConfig aws_specs = new AWSS3FileSystemConfig();
			aws_specs.setBucketName(bankSettings.s3_bucket_name);//
			final AWSS3FileSystem S3 = new AWSS3FileSystem(aws_specs);
			final File remote = S3.ROOT().child(bankSettings.s3_bucket_bank_folder_name);
			final File local = localBankFolder;

			final FileSystem FS = remote.getFileSystem();

			final ChildrenList toCopy = local.listDirectChildren();
			toCopy.print("deploy");
			for (final File f : toCopy) {
				final File twin = remote.child(f.getName());
				if (f.isFolder()) {
					twin.makeFolder();
				} else {
					FS.copyFileToFile(f, twin, FileConflistResolver.OVERWRITE_ON_HASH_MISMATCH);
				}
			}

// FS.copyFolderContentsToFolder(local, remote, FileConflistResolver.OVERWRITE_ON_HASH_MISMATCH);
// FS.copyFilesTo(toCopy, remote);
		}
		{
			final HttpURL url = bankSettings.toPublicURLString();
			L.d("checking remote bank", "" + url);
			final File assets_cache_folder = LocalFileSystem.ApplicationHome().child("assets-cache");
			assets_cache_folder.makeFolder();
			final RedHttpFileSystemSpecs http_specs = new RedHttpFileSystemSpecs();

			http_specs.setRootUrl(url);
			final RedHttpFileSystem fs = new RedHttpFileSystem(http_specs);
			final File httpRemote = fs.ROOT();

			httpRemote.listDirectChildren().print("children");

		}

	}

}
