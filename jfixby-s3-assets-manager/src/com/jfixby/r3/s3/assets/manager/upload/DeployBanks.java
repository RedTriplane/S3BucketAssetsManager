
package com.jfixby.r3.s3.assets.manager.upload;

import java.io.IOException;

import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.scarabei.amazon.aws.RedAWS;
import com.jfixby.scarabei.api.collections.Collections;
import com.jfixby.scarabei.api.collections.List;
import com.jfixby.scarabei.api.collections.Mapping;
import com.jfixby.scarabei.api.debug.Debug;
import com.jfixby.scarabei.api.desktop.ScarabeiDesktop;
import com.jfixby.scarabei.api.err.Err;
import com.jfixby.scarabei.api.file.File;
import com.jfixby.scarabei.api.file.FileConflistResolver;
import com.jfixby.scarabei.api.file.FileSystem;
import com.jfixby.scarabei.api.file.FilesList;
import com.jfixby.scarabei.api.file.LocalFileSystem;
import com.jfixby.scarabei.api.json.Json;
import com.jfixby.scarabei.api.log.L;
import com.jfixby.scarabei.api.net.http.Http;
import com.jfixby.scarabei.api.net.http.HttpFileSystem;
import com.jfixby.scarabei.api.net.http.HttpFileSystemSpecs;
import com.jfixby.scarabei.api.net.http.HttpURL;
import com.jfixby.scarabei.aws.api.AWS;
import com.jfixby.scarabei.aws.api.s3.S3FileSystem;
import com.jfixby.scarabei.aws.api.s3.S3FileSystemConfig;
import com.jfixby.scarabei.gson.GoogleGson;
import com.jfixby.tool.eclipse.dep.EclipseProjectInfo;
import com.jfixby.tool.eclipse.dep.EclipseWorkSpaceSettings;

public class DeployBanks {

	public static void main (final String[] args) throws IOException {

		ScarabeiDesktop.deploy();
		Json.installComponent(new GoogleGson());
		AWS.installComponent(new RedAWS());

		final Mapping<String, S3BankSettings> availableSettings = S3BankSettings.loadSettings();
		{
			final String bankName = "com.red-triplane.assets.r3";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// deploy(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.tinto";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// deploy(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.lib";
			final List<String> tanksToProcess = Collections.newList("tank-0");
// deploy(bankName, tanksToProcess, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.parallax";
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

		final File bankFolder = assetsFolder.child(bankSettings.local_bank_folder_name);
		deploy(bankFolder, bankSettings);

	}

	private static void deploy (final File localBankFolder, final S3BankSettings bankSettings) throws IOException {
		Debug.checkTrue(localBankFolder + " does not exist", localBankFolder.exists());

		final String bankName = localBankFolder.getName();
		{

			final S3FileSystemConfig aws_specs = AWS.getS3().newFileSystemConfig();
			aws_specs.setBucketName(bankSettings.s3_bucket_name);//
			final S3FileSystem S3 = AWS.getS3().newFileSystem(aws_specs);
			final File remote = S3.ROOT().child(bankSettings.s3_bucket_bank_folder_name);
			final File local = localBankFolder;

			final FileSystem FS = remote.getFileSystem();

			final FilesList toCopy = local.listDirectChildren();
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

			final HttpFileSystemSpecs http_specs = Http.newHttpFileSystemSpecs();

			http_specs.setRootUrl(url);
			final HttpFileSystem fs = Http.newHttpFileSystem(http_specs);
			final File httpRemote = fs.ROOT();

			httpRemote.listDirectChildren().print("children");

		}

	}

}
