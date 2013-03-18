__kernel void render_px(const int len,
        const int apply_gamma,
        __global int* pdata,
        __global const int* gtable) {

    int gid = get_global_id(0);
    if(gid >= len)
        return;
        
    int src_val = pdata[gid];
	int b = src_val & 0xFF;
	int g = src_val >> 8 & 0xFF;
	int r = src_val >> 16 & 0xFF;
	int a = src_val >>  24 & 0xFF;
	
	if(apply_gamma) {
	    a = gtable[a];
	    r = gtable[r];
	    g = gtable[g];
	    b = gtable[b];
	}
	
	src_val = a;
	src_val = (src_val << 8) + r;
	src_val = (src_val << 8) + g;
	src_val = (src_val << 8) + b;
	pdata[gid] = src_val;
	
}