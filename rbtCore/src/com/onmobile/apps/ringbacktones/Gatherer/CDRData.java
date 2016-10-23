package com.onmobile.apps.ringbacktones.Gatherer;

class CDRData
{

	int number_of_calls, NS_count, NU_count, UU_count,
	US_count;
	double call_volume, total_duration;
	int num_incircle_calls, num_outcircle_calls, num_mobile_calls,
	num_landline_calls;

	public CDRData()
	{

		call_volume = 0;
		number_of_calls = 0;
		NS_count = 0;
		NU_count = 0;
		UU_count = 0;
		US_count = 0;
		total_duration = 0;
		num_incircle_calls = 0;
		num_outcircle_calls = 0;
		num_mobile_calls = 0;
		num_landline_calls = 0;
	}
}

