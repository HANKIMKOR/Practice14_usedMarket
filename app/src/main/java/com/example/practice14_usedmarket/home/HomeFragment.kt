package com.example.practice14_usedmarket.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.practice14_usedmarket.DBKey.Companion.DB_ARTICLES
import com.example.practice14_usedmarket.DBKey.Companion.DB_USERS
import com.example.practice14_usedmarket.R
import com.example.practice14_usedmarket.chatlist.ChatListItem
import com.example.practice14_usedmarket.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var articleDB: DatabaseReference
    private lateinit var articleAdapter: ArticleAdapter

    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object : ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            TODO("Not yet implemented")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }

    private var binding: FragmentHomeBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        articleList.clear()
        userDB = Firebase.database.reference.child(DB_USERS)
        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleAdapter = ArticleAdapter(onItemClickd = { articleModel ->
            if (auth.currentUser != null) {
                // ???????????? ??? ??????
                if (auth.currentUser!!.uid != articleModel.sellerId) {
                    val chatRoom = articleModel.sellerId?.let {
                        ChatListItem(
                            buyerId = auth.currentUser!!.uid,
                            sellerId = it,
                            itemTitle = articleModel.title,
                            key = System.currentTimeMillis()
                        )
                    }

                } else {
                    // ?????? ?????? ?????????
                    Snackbar.make(view, "?????? ?????? ??????????????????.", Snackbar.LENGTH_LONG).show()
                }
            } else {
                // ???????????? ?????? ??????
                Snackbar.make(view, "????????? ??? ??????????????????", Snackbar.LENGTH_LONG).show()
            }


        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter
        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            context?.let {
                if (auth.currentUser != null) {
                    val intent = Intent(requireContext(), AddArticleActivity::class.java)
                    startActivity(intent)
                } else {
                    Snackbar.make(view, "????????? ??? ??????????????????", Snackbar.LENGTH_LONG).show()
                }
            }


//            //?????? ????????? ???????????? ???
//            val intent = Intent(requireContext(), AddArticleActivity::class.java)
//            startActivity(intent)
        }

        articleDB.addChildEventListener(listener)

    }

    override fun onResume() {
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        articleDB.removeEventListener(listener)
    }
}