package vn.thientf.iwaiter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.thientf.iwaiter.Fragment.FragmentMenu;
import vn.thientf.iwaiter.Fragment.FragmentUser;
import vn.thientf.iwaiter.Models.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_SCAN = 1111;

    DatabaseReference userRef;
    FirebaseUser user;

    NavigationView navigationView;

    String headerTitle;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int containerViewId = R.id.main_container;
    FloatingActionButton fabCart;
    View cartView;
    TextView tvUserName;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            this.startActivity(new Intent(getBaseContext(), LoginActivity.class));
            finish();
        } else {
            user = currentUser;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabCart=findViewById(R.id.fab_cart);
        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ActivityCart.class));
            }
        });
        cartView = findViewById(R.id.cart_button);
        //hide cart button
        cartView.setVisibility(View.INVISIBLE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        tvUserName = headerView.findViewById(R.id.tv_userName);
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        createGlobalUser();


        //user can't view menu until scan a valid table QR code
        disableNavItem(R.id.nav_menu);
    }

    private void disableNavItem(int nav_item) {
        MenuItem menuItem=navigationView.getMenu().findItem(nav_item);
        menuItem.setEnabled(false);
    }

    private void enableNavItem(int nav_menu) {
        navigationView.getMenu().findItem(nav_menu).setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        if (id == R.id.nav_scan) {
            // Scan table qr code
            // save ->res id, table id ->
            /*new fm menu (id, tb id)
            * replace (fm menu)
            * */

            //save resId & tableId to GlobalData
            //this will change fragment depend on the existing of resId in Database
            startActivityForResult(new Intent(getApplicationContext(),StartActivity.class),REQUEST_SCAN);
       //     checkRestaurantId(GlobalData.getInstance().getCurrRes());
        } else if (id == R.id.nav_menu) {
            cartView.setVisibility(View.VISIBLE);
            FragmentMenu fragmentMenu = new FragmentMenu();
            replaceFragment(fragmentMenu);
            changeTitle("Menu");
        } else if (id == R.id.nav_orders) {

        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_info) {
             fabCart.hide();
             FragmentUser fragmentUser = new FragmentUser();
             replaceFragment(fragmentUser);
             changeTitle("Info");
        } else if (id == R.id.nav_signout) {
            FirebaseAuth.getInstance().signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeTitle(String menu) {
    }

    void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.main_container,fragment)
                .commit();
    }

    void checkRestaurantId(final String resId) {
        if (resId == null)
            return;
        DatabaseReference restaurantRef = database.getReference(getString(R.string.RestaurantsRef));
        restaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(resId)) {
                    Toast.makeText(getBaseContext(), "Restaurant not exist!", Toast.LENGTH_LONG).show();
                } else {
                    //Open Menu of this Restaurant
                    enableNavItem(R.id.nav_menu);
                    fabCart.show();
                    getFragmentManager().beginTransaction()
                            .replace(containerViewId, new FragmentMenu())
                            .addToBackStack("menu")
                            .commit();
                    Toast.makeText(getBaseContext(), "Welcome <3", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_SCAN && resultCode==RESULT_OK){
            String qrcode = data.getStringExtra("result");
            validateQRCode(qrcode);

            Toast.makeText(this, qrcode,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void validateQRCode(String qrcode) {
        if(qrcode.startsWith("iWaiter@")){
            qrcode=qrcode.substring(8);
            String[] ids=qrcode.split("#");
            String resId=ids[0];
            String tableId=ids[1];
            if (!resId.isEmpty() && !tableId.isEmpty()){
                GlobalData.getInstance().setCurrRes(resId);
                GlobalData.getInstance().setCurrTable(tableId);
                checkRestaurantId(resId);
            }

        }
    }

    private void createGlobalUser() {
        //find user in DB
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user.getUid())) {
                    //if not exist, create new user
                    userRef.child(user.getUid()).setValue(new User(user));
                }
                User myUser = dataSnapshot.child(user.getUid()).getValue(User.class);
                GlobalData.getInstance().setCurrUser(myUser);
                tvUserName.setText(GlobalData.getInstance().getCurrUser().getPhone());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
