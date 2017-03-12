
package com.jfixby.r3.s3.assets.manager.upload;

import java.io.IOException;

import com.jfixby.r3.s3.assets.manager.EnvironmentConfig;
import com.jfixby.r3.s3.assets.manager.S3BankSettings;
import com.jfixby.scarabei.amazon.aws.RedAWS;
import com.jfixby.scarabei.api.collections.Mapping;
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

public class UploadRootsToS3 {

	public static void main (final String[] args) throws IOException {

		ScarabeiDesktop.deploy();
		Json.installComponent(new GoogleGson());
		AWS.installComponent(new RedAWS());

		final Mapping<String, S3BankSettings> availableSettings = S3BankSettings.loadSettings();
		{
			final String bankName = "com.red-triplane.assets.r3";
// upload(bankName, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.tinto";
			upload(bankName, availableSettings);
		}
		{
			final String bankName = "com.red-triplane.assets.lib";
// upload(bankName, availableSettings);
		}
	}

	private static void upload (final String bankName, final Mapping<String, S3BankSettings> availableSettings)
		throws IOException {

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
		final FilesList files = bankFolder.listDirectChildren(f -> {
			try {
				return f.isFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return false;
		});

		for (final File file : files) {
			upload(file, bankSettings);
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

			httpRemote.listDirectChildren().print("all-children");

		}
	}

	private static void upload (final File rootFile, final S3BankSettings bankSettings) throws IOException {
		{

			final S3FileSystemConfig aws_specs = AWS.getS3().newFileSystemConfig();
			aws_specs.setBucketName(bankSettings.s3_bucket_name);//
			final S3FileSystem S3 = AWS.getS3().newFileSystem(aws_specs);
			final File remote = S3.ROOT().child(bankSettings.s3_bucket_bank_folder_name);
			final File local = rootFile;

			final FileSystem FS = remote.getFileSystem();

// FS.copyFolderContentsToFolder(local, remote, FileConflistResolver.OVERWRITE_ON_HASH_MISMATCH);
			FS.copyFileToFolder(local, remote, FileConflistResolver.OVERWRITE_ON_HASH_MISMATCH);
		}

	}

}
