 package soft.stark.finalproject.fragments;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import soft.stark.finalproject.Adapters.ProductAdapter;
import soft.stark.finalproject.AsyncTask.InsertAsyncTask;
import soft.stark.finalproject.ProductModel;
import soft.stark.finalproject.R;
import soft.stark.finalproject.Room.RoomFactory;
import soft.stark.finalproject.webServices.ProductsResponse;
import soft.stark.finalproject.webServices.RetrofitFactory;
import soft.stark.finalproject.webServices.WebServices;

 public class HomeFragment extends Fragment {

    private RecyclerView productRv;
    private List<ProductModel> productList =new ArrayList();
    private ProductAdapter productAdapter;

    private WebServices webServices;
    private ProgressDialog dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_home, container, false);
        productRv=view.findViewById(R.id.home_rv);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpProgressDialog();
        dialog.show();
        setupRecyclerView();

        callProductsAPI();

    }

     private void setUpProgressDialog() {

        dialog=new ProgressDialog(requireContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


     }

     private void callProductsAPI() {

        webServices = RetrofitFactory.getRetrofit().create(WebServices.class);
         Call<ProductsResponse>getProducts=webServices.getProducts();
         getProducts.enqueue(new Callback<ProductsResponse>() {
             @Override
             public void onResponse(Call<ProductsResponse> call, Response<ProductsResponse> response) {
                 dialog.dismiss();
                 productList.clear();
                 productList.addAll(response.body().getProductList());
                 productAdapter.notifyDataSetChanged();
             }

             @Override
             public void onFailure(Call<ProductsResponse> call, Throwable t) {
                 dialog.dismiss();
                 Toast.makeText(requireContext(), "Error Happend", Toast.LENGTH_SHORT).show();
                 Log.d("Error",t.toString());
             }
         });


     }

     private void setupRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(),2);
        productRv.setLayoutManager(layoutManager);
        productRv.addItemDecoration(new GridSpacingItemDecoration(2,dpToPx(10),true));
        productRv.setItemAnimator(new DefaultItemAnimator());

        productAdapter =new ProductAdapter(productList, requireContext(), new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(View view, int position) {
                ProductModel selectedModel = productList.get(position);
                Bundle bundle = new Bundle();
                bundle.putSerializable("current_product", selectedModel);
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_productDetailsFragment, bundle);
            }
        }, new ProductAdapter.onAddProductsClickListener() {
            @Override
            public void onAddProductClick(View view, int position) {
                ProductModel productModel = productList.get(position);
                productModel.setQuantity(1);
                new InsertAsyncTask(RoomFactory.getRoomDatabase(requireContext()).getProductDao()).execute(productModel);
                Navigation.findNavController(view).navigate(R.id.action_homeFragment_to_cartFragment);
            }
        });
        productRv.setAdapter(productAdapter);
        productAdapter.notifyDataSetChanged();
    }



    //GridSpacingItemDecoration
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }

    }
    public int dpToPx(final float dp) {
        return Math.round(dp * (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    //End GridSpacingItemDecoration
}


