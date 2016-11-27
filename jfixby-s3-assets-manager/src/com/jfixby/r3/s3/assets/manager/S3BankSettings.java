
package com.jfixby.r3.s3.assets.manager;

import java.util.ArrayList;

public class S3BankSettings {

	public String bank_name = "";

	public String s3_bucket_name;

	public String s3_bucket_bank_folder_name;

	public String s3_bucket_host;

	public ArrayList<TankInfo> tanks = new ArrayList<>();

}
